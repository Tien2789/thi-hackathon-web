package com.ontop.wms.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontop.wms.dto.ApproveRequest;
import com.ontop.wms.dto.InboundDTO;
import com.ontop.wms.dto.InventoryRequest;
import com.ontop.wms.dto.OutboundDTO;
import com.ontop.wms.entity.InDetail;
import com.ontop.wms.entity.InventoryIn;
import com.ontop.wms.entity.InventoryOut;
import com.ontop.wms.entity.OutDetail;
import com.ontop.wms.entity.Product;
import com.ontop.wms.repository.InDetailRepository;
import com.ontop.wms.repository.InventoryInRepository;
import com.ontop.wms.repository.InventoryOutRepository;
import com.ontop.wms.repository.OutDetailRepository;
import com.ontop.wms.repository.ProductRepository;
import com.ontop.wms.repository.WarehouseRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryInRepository inventoryInRepository;
    private final InventoryOutRepository inventoryOutRepository;
    private final ProductRepository productRepository;
    private final InDetailRepository inDetailRepository;
    private final OutDetailRepository outDetailRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    public List<InboundDTO> getAllInbounds() {
        return inventoryInRepository.findAll().stream()
                .map(InboundDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventoryIn createInbound(InventoryRequest request) {
        InventoryIn inventoryIn = new InventoryIn();
        if (request != null) {
            inventoryIn.setReceiptCode(request.getReceiptCode());
            inventoryIn.setStatus("PENDING");
            inventoryIn.setCreatedAt(new Timestamp(Instant.now().toEpochMilli()));
            Integer warehouseId = request.getWarehouseId();
            if (warehouseId != null) {
                warehouseRepository.findById(warehouseId)
                    .ifPresent(inventoryIn::setWarehouse);
            }
        }
        return inventoryInRepository.save(inventoryIn);
    }

    @Override
    @Transactional
    public InventoryIn approveInbound(Integer id, ApproveRequest request) {
        InventoryIn inventoryIn = inventoryInRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Phiếu nhập không tồn tại với id: " + id));

        if (!"PENDING".equals(inventoryIn.getStatus())) {
            throw new IllegalStateException("Chỉ có thể duyệt phiếu nhập ở trạng thái PENDING.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại với id: " + request.getProductId()));

        product.setCurrentStock(product.getCurrentStock() + request.getQuantity());
        productRepository.save(product);

        InDetail detail = new InDetail();
        detail.setInventoryIn(inventoryIn);
        detail.setProduct(product);
        detail.setQuantity(request.getQuantity());
        inDetailRepository.save(detail);

        inventoryIn.setStatus("APPROVED");
        return inventoryInRepository.save(inventoryIn);
    }

    @Override
    public List<OutboundDTO> getAllOutbounds() {
        return inventoryOutRepository.findAll().stream()
                .map(OutboundDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventoryOut createOutbound(InventoryRequest request) {
        InventoryOut inventoryOut = new InventoryOut();
        if (request != null) {
            inventoryOut.setIssueCode(request.getIssueCode());
            inventoryOut.setStatus("PENDING");
            inventoryOut.setCreatedAt(new Timestamp(Instant.now().toEpochMilli()));
            Integer warehouseId = request.getWarehouseId();
            if (warehouseId != null) {
                warehouseRepository.findById(warehouseId)
                    .ifPresent(inventoryOut::setWarehouse);
            }
        }
        return inventoryOutRepository.save(inventoryOut);
    }

    @Override
    @Transactional
    public InventoryOut approveOutbound(Integer id, ApproveRequest request) {
        InventoryOut inventoryOut = inventoryOutRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Phiếu xuất không tồn tại với id: " + id));

        if (!"PENDING".equals(inventoryOut.getStatus())) {
            throw new IllegalStateException("Chỉ có thể duyệt phiếu xuất ở trạng thái PENDING.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại với id: " + request.getProductId()));

        if (product.getCurrentStock() < request.getQuantity()) {
            throw new IllegalStateException("Không đủ tồn kho cho sản phẩm: " + product.getProductName());
        }

        product.setCurrentStock(product.getCurrentStock() - request.getQuantity());
        productRepository.save(product);

        OutDetail detail = new OutDetail();
        detail.setInventoryOut(inventoryOut);
        detail.setProduct(product);
        detail.setQuantity(request.getQuantity());
        outDetailRepository.save(detail);

        inventoryOut.setStatus("APPROVED");
        return inventoryOutRepository.save(inventoryOut);
    }

    @Override
    @Transactional
    public InventoryOut undoOutbound(Integer id) {
        if (id == null) {
             throw new IllegalArgumentException("ID cannot be null");
        }
        InventoryOut inventoryOut = inventoryOutRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Outbound not found with id: " + id));

        if (!"APPROVED".equals(inventoryOut.getStatus())) {
            throw new IllegalStateException("Cannot undo an outbound that is not approved.");
        }

        OutDetail detail = outDetailRepository.findByInventoryOut(inventoryOut)
                .orElseThrow(() -> new IllegalStateException("No details found for this outbound to undo."));

        Product product = detail.getProduct();
        product.setCurrentStock(product.getCurrentStock() + detail.getQuantity());
        productRepository.save(product);

        inventoryOut.setStatus("CANCELLED");
        return inventoryOutRepository.save(inventoryOut);
    }
}