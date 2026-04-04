package com.ontop.wms.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import com.ontop.wms.entity.InventoryOut;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundDTO {
    private Integer id;
    private String issueCode;
    private String status;
    private Timestamp createdAt;
    private String warehouseName;
    
    // Circular 200 Fields
    private String receiverName;
    private String reason;
    private String documentNumber;
    
    private List<OutDetailDTO> details;

    @Data
    @AllArgsConstructor
    public static class OutDetailDTO {
        private String productName;
        private String skuCode;
        private String unitName;
        private Integer requestedQuantity;
        private Integer actualQuantity;
        private BigDecimal unitPrice;
    }

    public static OutboundDTO fromEntity(InventoryOut entity) {
        OutboundDTO dto = new OutboundDTO();
        dto.setId(entity.getId());
        dto.setIssueCode(entity.getIssueCode());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setWarehouseName(entity.getWarehouse() != null ? entity.getWarehouse().getName() : null);
        dto.setReceiverName(entity.getReceiverName());
        dto.setReason(entity.getReason());
        dto.setDocumentNumber(entity.getDocumentNumber());
        
        if (entity.getDetails() != null) {
            dto.setDetails(entity.getDetails().stream()
                .map(d -> new OutDetailDTO(
                    d.getProduct().getProductName(),
                    d.getProduct().getSkuCode(),
                    d.getProduct().getUnit() != null ? d.getProduct().getUnit().getName() : "Cái",
                    d.getRequestedQuantity(),
                    d.getActualQuantity(),
                    d.getUnitPrice()
                ))
                .collect(Collectors.toList()));
        }
        return dto;
    }
}