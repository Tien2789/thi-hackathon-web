package com.ontop.wms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontop.wms.entity.InDetail;
import com.ontop.wms.entity.Notification;
import com.ontop.wms.repository.InDetailRepository;
import com.ontop.wms.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final InDetailRepository inDetailRepository;
    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 1 * * ?") // Every day at 1 AM
    @Transactional
    public void checkStorageLimits() {
        List<InDetail> stocks = inDetailRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (InDetail stock : stocks) {
            if (stock.getRemainingQuantity() > 0) {
                int limitMonths = stock.getProduct().getStorageLimitMonths();
                LocalDateTime inboundDate = stock.getInventoryIn().getCreatedAt().toLocalDateTime();
                
                if (inboundDate.plusMonths(limitMonths).isBefore(now)) {
                    createNotification(stock);
                }
            }
        }
    }

    private void createNotification(InDetail stock) {
        String msg = String.format("Sản phẩm %s (Lô #%d) đã lưu kho quá %d tháng.", 
            stock.getProduct().getProductName(), 
            stock.getInventoryIn().getId(),
            stock.getProduct().getStorageLimitMonths());
        
        if (!notificationRepository.existsByMessageAndIsReadFalse(msg)) {
            Notification note = new Notification();
            note.setMessage(msg);
            note.setType("STORAGE_LIMIT");
            note.setRead(false);
            note.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(note);
        }
    }
}
