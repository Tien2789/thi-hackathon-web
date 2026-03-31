package com.ontop.wms.service;

import com.ontop.wms.entity.*;
import com.ontop.wms.repository.*;
import com.ontop.wms.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InDetailRepository inDetailRepository;
    private final ProductRepository productRepository;
    private final SignatureRepository signatureRepository;
    private final InventoryInRepository inventoryInRepository;
    private final InventoryOutRepository inventoryOutRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InboundDTO> getAllInbounds() {
        return inventoryInRepository.findAll().stream()
                .map(this::convertToInboundDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboundDTO> getAllOutbounds() {
        return inventoryOutRepository.findAll().stream()
                .map(this::convertToOutboundDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventoryIn createInbound(InventoryRequest request) {
        InventoryIn inbound = new InventoryIn();
        inbound.setReceiptCode(request.getReceiptCode());
        inbound.setStatus("PENDING");
        // Logic khởi tạo signature flow từ request.getSignerEmails()
        InventoryIn saved = inventoryInRepository.save(inbound);
        initSignatureFlow(saved, request.getSignerEmails());
        return saved;
    }

    /**
     * Logic Phê duyệt Nhập kho (Tạo lô hàng)
     */
    @Override
    @Transactional
    public InventoryIn approveInbound(Integer inboundId, ApproveRequest request) {
        InventoryIn inbound = inventoryInRepository.findById(inboundId).orElseThrow();
        Product product = productRepository.findById(request.getProductId()).orElseThrow();

        // Tạo chi tiết lô hàng (InDetail) để quản lý FIFO sau này
        InDetail lot = new InDetail();
        lot.setInventoryIn(inbound);
        lot.setProduct(product);
        // lot.setQuantityActual(request.getQuantity()); // Nếu Entity chưa có method này, hãy dùng quantity
        lot.setQuantity(request.getQuantity()); 
        lot.setRemainingQuantity(request.getQuantity());
        lot.setUnitPrice(request.getUnitPrice());
        lot.setReceivedDate(LocalDate.now());
        lot.setExpiryDate(request.getExpiryDate());

        inDetailRepository.save(lot);

        // Cập nhật tổng tồn kho của sản phẩm
        product.setCurrentStock(product.getCurrentStock() + request.getQuantity());
        productRepository.save(product);
        
        inbound.setStatus("APPROVED");
        return inventoryInRepository.save(inbound);
    }

    @Override
    public InventoryOut createOutbound(InventoryRequest request) {
        InventoryOut outbound = new InventoryOut();
        outbound.setIssueCode(request.getIssueCode());
        outbound.setStatus("PENDING");
        return inventoryOutRepository.save(outbound);
    }

    /**
     * Logic Phê duyệt Xuất kho theo nguyên tắc FIFO (Vô trước - Ra trước)
     */
    @Override
    @Transactional
    public InventoryOut approveOutbound(Integer outboundId, ApproveRequest request) {
        Product product = productRepository.findById(request.getProductId()).orElseThrow();
        if (product.getCurrentStock() < request.getQuantity()) throw new RuntimeException("Không đủ tồn kho");

        // Tìm các lô hàng cũ nhất của sản phẩm này còn tồn (FIFO)
        List<InDetail> oldestLots = inDetailRepository
            .findByProductAndRemainingQuantityGreaterThanOrderByReceivedDateAsc(product, 0);

        int remainingToIssue = request.getQuantity();
        for (InDetail lot : oldestLots) {
            if (remainingToIssue <= 0) break;

            int takeFromThisLot = Math.min(lot.getRemainingQuantity(), remainingToIssue);
            lot.setRemainingQuantity(lot.getRemainingQuantity() - takeFromThisLot);
            remainingToIssue -= takeFromThisLot;
            
            inDetailRepository.save(lot);
        }

        product.setCurrentStock(product.getCurrentStock() - request.getQuantity());
        productRepository.save(product);

        InventoryOut outbound = inventoryOutRepository.findById(outboundId).orElseThrow();
        outbound.setStatus("APPROVED");
        return inventoryOutRepository.save(outbound);
    }

    @Override
    @Transactional
    public InventoryOut undoOutbound(Integer outboundId) {
        InventoryOut outbound = inventoryOutRepository.findById(outboundId).orElseThrow();
        outbound.setStatus("CANCELLED");
        return inventoryOutRepository.save(outbound);
    }

    /**
     * Khởi tạo quy trình ký số (Digital Workflow)
     */
    public void initSignatureFlow(InventoryIn inbound, List<String> emails) {
        if (emails == null) return;
        emails.forEach(email -> {
            Signature sig = new Signature();
            sig.setSignerEmail(email);
            sig.setSignatureToken(UUID.randomUUID().toString());
            sig.setSigned(false);
            sig.setInventoryIn(inbound); // Đảm bảo Signature.java có @ManyToOne InventoryIn inventoryIn
            signatureRepository.save(sig);
        });
    }

    @Override
    public Signature getSignatureByToken(String token) {
        return signatureRepository.findBySignatureToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));
    }

    @Override
    @Transactional
    public Signature confirmSignature(String token) {
        Signature sig = getSignatureByToken(token);
        sig.setSigned(true);
        sig.setSignedAt(java.time.LocalDateTime.now());
        return signatureRepository.save(sig);
    }

    private InboundDTO convertToInboundDto(InventoryIn entity) {
        InboundDTO dto = new InboundDTO();
        dto.setId(entity.getId());
        dto.setReceiptCode(entity.getReceiptCode());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        // Map thêm các trường delivererName, documentNumber nếu có
        return dto;
    }

    private OutboundDTO convertToOutboundDto(InventoryOut entity) {
        OutboundDTO dto = new OutboundDTO();
        dto.setId(entity.getId());
        dto.setIssueCode(entity.getIssueCode());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}