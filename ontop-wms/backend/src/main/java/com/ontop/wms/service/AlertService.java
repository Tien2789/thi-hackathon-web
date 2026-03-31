package com.ontop.wms.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontop.wms.entity.InDetail;
import com.ontop.wms.entity.Notification;
import com.ontop.wms.entity.User;
import com.ontop.wms.repository.InDetailRepository;
import com.ontop.wms.repository.NotificationRepository;
import com.ontop.wms.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final InDetailRepository inDetailRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // Run every day at 1 AM
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void checkStorageAlerts() {
        log.info("Starting storage limit and expiry check...");
        
        List<InDetail> activeBatches = inDetailRepository.findAll(); // Simple for now, can be optimized
        List<User> managers = userRepository.findAll(); // Notify all managers/admins

        for (InDetail batch : activeBatches) {
            if (batch.getRemainingQuantity() <= 0) continue;

            // 1. Check Storage Limit (Obsolescence)
            long daysInStock = ChronoUnit.DAYS.between(batch.getReceivedDate(), java.time.LocalDate.now());
            Integer limit = batch.getProduct().getStorageLimitDays();
            if (limit != null && daysInStock > limit) {
                String message = String.format("CẢNH BÁO: Sản phẩm [%s] (Lô: %s) đã lưu kho %d ngày, vượt quá hạn mức %d ngày.", 
                    batch.getProduct().getProductName(), batch.getInventoryIn().getReceiptCode(), daysInStock, limit);
                createAlertNotifications(message, "STORAGE_LIMIT", managers);
            }

            // 2. Check Expiry Date
            if (batch.getExpiryDate() != null) {
                long daysToExpiry = ChronoUnit.DAYS.between(LocalDateTime.now(), batch.getExpiryDate().atStartOfDay());
                if (daysToExpiry <= 30 && daysToExpiry > 0) {
                    String message = String.format("CẢNH BÁO: Sản phẩm [%s] (Lô: %s) sắp hết hạn sau %d ngày (%s).",
                        batch.getProduct().getProductName(), batch.getInventoryIn().getReceiptCode(), daysToExpiry, batch.getExpiryDate());
                    createAlertNotifications(message, "EXPIRY_NEAR", managers);
                } else if (daysToExpiry <= 0) {
                    String message = String.format("CẢNH BÁO NGHIÊM TRỌNG: Sản phẩm [%s] (Lô: %s) ĐÃ HẾT HẠN (%s)!",
                        batch.getProduct().getProductName(), batch.getInventoryIn().getReceiptCode(), batch.getExpiryDate());
                    createAlertNotifications(message, "EXPIRED", managers);
                }
            }
        }
    }

    private void createAlertNotifications(String message, String type, List<User> users) {
        log.warn("Alert generated: {}", message);
        for (User user : users) {
            // Avoid duplicate notifications for the same message in the same day if possible
            // For hackathon, just create one
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType(type);
            notification.setMessage(message);
            notification.setIsRead(false);
            notificationRepository.save(notification);
            
            // Optionally send email
            if (user.getEmail() != null) {
                emailService.sendSimpleEmail(user.getEmail(), "Cảnh báo kho: " + type, message);
            }
        }
    }
}
