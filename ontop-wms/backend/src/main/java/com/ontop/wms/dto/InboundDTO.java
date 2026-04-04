package com.ontop.wms.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import com.ontop.wms.entity.InventoryIn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboundDTO {
    private Integer id;
    private String receiptCode;
    private String status;
    private Timestamp createdAt;
    private String warehouseName;
    
    // Circular 200 Fields
    private String delivererName;
    private String documentNumber;
    private String location;
    
    private List<DetailDTO> details;

    @Data
    @AllArgsConstructor
    public static class DetailDTO {
        private String productName;
        private String skuCode;
        private String unitName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }

    public static InboundDTO fromEntity(InventoryIn entity) {
        InboundDTO dto = new InboundDTO();
        dto.setId(entity.getId());
        dto.setReceiptCode(entity.getReceiptCode());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setWarehouseName(entity.getWarehouse() != null ? entity.getWarehouse().getName() : null);
        dto.setDelivererName(entity.getDelivererName());
        dto.setDocumentNumber(entity.getDocumentNumber());
        dto.setLocation(entity.getLocation());
        
        if (entity.getDetails() != null) {
            dto.setDetails(entity.getDetails().stream()
                .map(d -> new DetailDTO(
                    d.getProduct().getProductName(),
                    d.getProduct().getSkuCode(),
                    d.getProduct().getUnit() != null ? d.getProduct().getUnit().getName() : "Cái",
                    d.getQuantity(),
                    d.getUnitPrice()
                ))
                .collect(Collectors.toList()));
        }
        return dto;
    }
}