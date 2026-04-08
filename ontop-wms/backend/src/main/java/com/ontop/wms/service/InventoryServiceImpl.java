package com.ontop.wms.service;

import java.math.BigDecimal;
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
import com.ontop.wms.repository.CategoryRepository;
import com.ontop.wms.repository.UnitRepository;

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
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final SignatureService signatureService;

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
            String code = request.getReceiptCode();
            if (code == null || code.isEmpty()) {
                code = "PNK-" + System.currentTimeMillis();
            }
            inventoryIn.setReceiptCode(code);
            inventoryIn.setDelivererName(request.getDelivererName());
            inventoryIn.setDocumentNumber(request.getDocumentNumber());
            inventoryIn.setLocation(request.getLocation());
            inventoryIn.setStatus("PENDING");
            inventoryIn.setCreatedAt(new Timestamp(Instant.now().toEpochMilli()));
            
            if (request.getWarehouseId() != null) {
                warehouseRepository.findById(request.getWarehouseId().longValue()).ifPresent(inventoryIn::setWarehouse);
            }
            inventoryIn = inventoryInRepository.save(inventoryIn);

            if (request.getDetails() != null && !request.getDetails().isEmpty()) {
                for (ApproveRequest item : request.getDetails()) {
                    Product targetProduct;
                    
                    if (item.getProductId() != null) {
                        targetProduct = productRepository.findById(item.getProductId().longValue())
                            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + item.getProductId()));
                    } else if (item.getProductName() != null) {
                        Product newProduct = new Product();
                        newProduct.setProductName(item.getProductName());
                        
                        String sku = item.getSkuCode();
                        if (sku == null || sku.isEmpty()) {
                            sku = generateSku(item.getProductName());
                        }
                        newProduct.setSkuCode(sku);
                        newProduct.setBarcode(item.getBarcode() != null ? item.getBarcode() : sku);
                        newProduct.setCurrentStock(0);

                        if (item.getCategoryId() != null) {
                            categoryRepository.findById(item.getCategoryId().longValue()).ifPresent(newProduct::setCategory);
                        } else if (item.getCategoryName() != null && !item.getCategoryName().isEmpty()) {
                            categoryRepository.findByName(item.getCategoryName())
                                .ifPresentOrElse(newProduct::setCategory, () -> {
                                    com.ontop.wms.entity.Category newCat = new com.ontop.wms.entity.Category();
                                    newCat.setName(item.getCategoryName());
                                    newProduct.setCategory(categoryRepository.save(newCat));
                                });
                        }

                        if (item.getUnitId() != null) {
                            unitRepository.findById(item.getUnitId().longValue()).ifPresent(newProduct::setUnit);
                        } else if (item.getUnitName() != null && !item.getUnitName().isEmpty()) {
                            unitRepository.findByName(item.getUnitName())
                                .ifPresentOrElse(newProduct::setUnit, () -> {
                                    com.ontop.wms.entity.Unit newUnit = new com.ontop.wms.entity.Unit();
                                    newUnit.setName(item.getUnitName());
                                    newProduct.setUnit(unitRepository.save(newUnit));
                                });
                        }
                        
                        targetProduct = productRepository.save(newProduct);
                    } else {
                        throw new IllegalArgumentException("Hoặc productId hoặc thông tin sản phẩm mới phải được cung cấp.");
                    }
                    
                    InDetail detail = new InDetail();
                    detail.setInventoryIn(inventoryIn);
                    detail.setProduct(targetProduct);
                    detail.setQuantity(item.getQuantity());
                    detail.setRemainingQuantity(item.getQuantity());
                    detail.setUnitPrice(item.getUnitPrice());
                    detail.setManufacturingDate(item.getManufacturingDate());
                    detail.setExpiryDate(item.getExpiryDate());
                    inDetailRepository.save(detail);

                    int currentStock = targetProduct.getCurrentStock() != null ? targetProduct.getCurrentStock() : 0;
                    int quantityToAdd = item.getQuantity() != null ? item.getQuantity() : 0;
                    targetProduct.setCurrentStock(currentStock + quantityToAdd);
                    productRepository.save(targetProduct);
                }
                inventoryIn.setStatus("APPROVED");
                inventoryInRepository.save(inventoryIn);
                
                if (inventoryIn.getId() != null) {
                    signatureService.initiateSignatures("INBOUND", inventoryIn.getId().intValue(), request.getSignerEmails());
                }
            }
        }
        return inventoryIn;
    }

    private String generateSku(String productName) {
        String prefix = "SKU";
        if (productName != null && productName.length() >= 3) {
            prefix = productName.substring(0, 3).toUpperCase();
        }
        return prefix + "-" + System.currentTimeMillis() % 1000000;
    }

    @Override
    @Transactional
    public InventoryIn approveInbound(Integer id, ApproveRequest request) {
        InventoryIn inventoryIn = inventoryInRepository.findById(id.longValue())
                .orElseThrow(() -> new EntityNotFoundException("Phiếu nhập không tồn tại với id: " + id));

        if (!"PENDING".equals(inventoryIn.getStatus())) {
            throw new IllegalStateException("Chỉ có thể duyệt phiếu nhập ở trạng thái PENDING.");
        }

        Product product = productRepository.findById( request.getProductId() != null ? request.getProductId().longValue() : null)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại với id: " + request.getProductId()));

        InDetail detail = new InDetail();
        detail.setInventoryIn(inventoryIn);
        detail.setProduct(product);
        detail.setQuantity(request.getQuantity());
        detail.setRemainingQuantity(request.getQuantity());
        detail.setUnitPrice(request.getUnitPrice());
        detail.setManufacturingDate(request.getManufacturingDate());
        detail.setExpiryDate(request.getExpiryDate());
        inDetailRepository.save(detail);

        int quantity = request.getQuantity() != null ? request.getQuantity() : 0;
        product.setCurrentStock((product.getCurrentStock() != null ? product.getCurrentStock() : 0) + quantity);
        productRepository.save(product);

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
            String code = request.getIssueCode();
            if (code == null || code.isEmpty()) {
                code = "PXK-" + System.currentTimeMillis();
            }
            inventoryOut.setIssueCode(code);
            inventoryOut.setReceiverName(request.getReceiverName());
            inventoryOut.setReason(request.getReason());
            inventoryOut.setDocumentNumber(request.getDocumentNumber());
            inventoryOut.setStatus("PENDING");
            inventoryOut.setCreatedAt(new Timestamp(Instant.now().toEpochMilli()));
            
            if (request.getWarehouseId() != null) {
                warehouseRepository.findById(request.getWarehouseId().longValue()).ifPresent(inventoryOut::setWarehouse);
            }
            inventoryOut = inventoryOutRepository.save(inventoryOut);

            if (request.getDetails() != null && !request.getDetails().isEmpty()) {
                for (ApproveRequest item : request.getDetails()) {
                    Product targetProduct;
                    
                    if (item.getProductId() != null) {
                        targetProduct = productRepository.findById(item.getProductId().longValue())
                            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + item.getProductId()));
                    } else if (item.getProductName() != null) {
                        Product newProduct = new Product();
                        newProduct.setProductName(item.getProductName());
                        
                        String sku = item.getSkuCode();
                        if (sku == null || sku.isEmpty()) {
                            sku = generateSku(item.getProductName());
                        }
                        newProduct.setSkuCode(sku);
                        newProduct.setBarcode(item.getBarcode() != null ? item.getBarcode() : sku);
                        newProduct.setCurrentStock(0);

                        if (item.getCategoryId() != null) {
                            categoryRepository.findById(item.getCategoryId().longValue()).ifPresent(newProduct::setCategory);
                        } else if (item.getCategoryName() != null && !item.getCategoryName().isEmpty()) {
                            categoryRepository.findByName(item.getCategoryName())
                                .ifPresentOrElse(newProduct::setCategory, () -> {
                                    com.ontop.wms.entity.Category newCat = new com.ontop.wms.entity.Category();
                                    newCat.setName(item.getCategoryName());
                                    newProduct.setCategory(categoryRepository.save(newCat));
                                });
                        }

                        if (item.getUnitId() != null) {
                            unitRepository.findById(item.getUnitId().longValue()).ifPresent(newProduct::setUnit);
                        } else if (item.getUnitName() != null && !item.getUnitName().isEmpty()) {
                            unitRepository.findByName(item.getUnitName())
                                .ifPresentOrElse(newProduct::setUnit, () -> {
                                    com.ontop.wms.entity.Unit newUnit = new com.ontop.wms.entity.Unit();
                                    newUnit.setName(item.getUnitName());
                                    newProduct.setUnit(unitRepository.save(newUnit));
                                });
                        }
                        
                        targetProduct = productRepository.save(newProduct);
                    } else {
                        throw new IllegalArgumentException("Hoặc productId hoặc thông tin sản phẩm mới phải được cung cấp.");
                    }
                    
                    int currentStock = targetProduct.getCurrentStock() != null ? targetProduct.getCurrentStock() : 0;
                    int quantityToExit = item.getQuantity() != null ? item.getQuantity() : 0;
                    if (currentStock < quantityToExit) {
                        throw new IllegalStateException("Insufficient stock for: " + targetProduct.getProductName());
                    }

                    int remainingToPick = quantityToExit;
                    List<InDetail> inDetails = inDetailRepository.findAvailableStockForFIFO(targetProduct);
                    for (InDetail inStock : inDetails) {
                        if (remainingToPick <= 0) break;
                        int pickAmount = Math.min(inStock.getRemainingQuantity(), remainingToPick);
                        inStock.setRemainingQuantity(inStock.getRemainingQuantity() - pickAmount);
                        inDetailRepository.save(inStock);
                        remainingToPick -= pickAmount;
                    }

                    if (remainingToPick > 0) throw new IllegalStateException("FIFO error for: " + targetProduct.getProductName());

                    targetProduct.setCurrentStock(currentStock - quantityToExit);
                    productRepository.save(targetProduct);

                    OutDetail detail = new OutDetail();
                    detail.setInventoryOut(inventoryOut);
                    detail.setProduct(targetProduct);
                    detail.setQuantity(item.getQuantity());
                    detail.setRequestedQuantity(item.getRequestedQuantity() != null ? item.getRequestedQuantity() : item.getQuantity());
                    detail.setActualQuantity(item.getQuantity());
                    detail.setUnitPrice(item.getUnitPrice());
                    outDetailRepository.save(detail);
                }
                inventoryOut.setStatus("APPROVED");
                inventoryOutRepository.save(inventoryOut);

                if (inventoryOut.getId() != null) {
                    signatureService.initiateSignatures("OUTBOUND", inventoryOut.getId().intValue(), request.getSignerEmails());
                }
            }
        }
        return inventoryOut;
    }

    @Override
    @Transactional
    public InventoryOut approveOutbound(Integer id, ApproveRequest request) {
        InventoryOut inventoryOut = inventoryOutRepository.findById(id.longValue())
                .orElseThrow(() -> new EntityNotFoundException("Phiếu xuất không tồn tại với id: " + id));

        if (!"PENDING".equals(inventoryOut.getStatus())) {
            throw new IllegalStateException("Chỉ có thể duyệt phiếu xuất ở trạng thái PENDING.");
        }

        Product product = productRepository.findById(request.getProductId() != null ? request.getProductId().longValue() : null)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại với id: " + request.getProductId()));

        int quantity = request.getQuantity() != null ? request.getQuantity() : 0;
        if ((product.getCurrentStock() != null ? product.getCurrentStock() : 0) < quantity) {
            throw new IllegalStateException("Không đủ tồn kho cho sản phẩm: " + product.getProductName());
        }

        int remainingToPick = quantity;
        List<InDetail> inDetails = inDetailRepository.findAvailableStockForFIFO(product);
        
        for (InDetail inStock : inDetails) {
            if (remainingToPick <= 0) break;
            int pickAmount = Math.min(inStock.getRemainingQuantity(), remainingToPick);
            inStock.setRemainingQuantity(inStock.getRemainingQuantity() - pickAmount);
            inDetailRepository.save(inStock);
            remainingToPick -= pickAmount;
        }

        if (remainingToPick > 0) {
            throw new IllegalStateException("Lỗi FIFO: Không tìm thấy lô hàng để đáp ứng đủ số lượng.");
        }

        product.setCurrentStock((product.getCurrentStock() != null ? product.getCurrentStock() : 0) - quantity);
        productRepository.save(product);

        OutDetail detail = new OutDetail();
        detail.setInventoryOut(inventoryOut);
        detail.setProduct(product);
        detail.setQuantity(quantity);
        detail.setRequestedQuantity(request.getRequestedQuantity());
        detail.setActualQuantity(quantity);
        detail.setUnitPrice(inDetails.isEmpty() ? BigDecimal.ZERO : inDetails.get(0).getUnitPrice());
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
        InventoryOut inventoryOut = inventoryOutRepository.findById(id.longValue())
                .orElseThrow(() -> new EntityNotFoundException("Outbound not found with id: " + id));

        if (!"APPROVED".equals(inventoryOut.getStatus())) {
            throw new IllegalStateException("Cannot undo an outbound that is not approved.");
        }

        List<OutDetail> details = outDetailRepository.findByInventoryOut(inventoryOut);
        if (details.isEmpty()) {
             throw new IllegalStateException("No details found for this outbound to undo.");
        }

        for (OutDetail detail : details) {
            Product product = detail.getProduct();
            Integer actualQuantity = detail.getActualQuantity();
            if (product != null && actualQuantity != null) {
                product.setCurrentStock((product.getCurrentStock() != null ? product.getCurrentStock() : 0) + actualQuantity);
                productRepository.save(product);
            }
        }

        inventoryOut.setStatus("CANCELLED");
        return inventoryOutRepository.save(inventoryOut);
    }
    
    @Override
    public void undoInbound(Integer id) {
        InventoryIn inventoryIn = inventoryInRepository.findById(id.longValue())
            .orElseThrow(() -> new EntityNotFoundException("Inbound not found with id: " + id));

        if (!"APPROVED".equals(inventoryIn.getStatus())) {
            throw new IllegalStateException("Cannot undo an inbound that is not approved.");
        }

        List<InDetail> details = inDetailRepository.findByInventoryIn(inventoryIn);
        for (InDetail detail : details) {
            Product product = detail.getProduct();
            Integer quantity = detail.getQuantity();
            if (product != null && quantity != null) {
                product.setCurrentStock(product.getCurrentStock() - quantity);
                productRepository.save(product);
            }
        }

        inventoryIn.setStatus("CANCELLED");
        inventoryInRepository.save(inventoryIn);
    }
}
