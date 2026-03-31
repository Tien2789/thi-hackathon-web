package com.ontop.wms.dto;

import java.sql.Timestamp;

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
    private String delivererName;
    private String documentNumber;

    public static InboundDTO fromEntity(InventoryIn entity) {
        return new InboundDTO(
                entity.getId(),
                entity.getReceiptCode(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getWarehouse() != null ? entity.getWarehouse().getName() : null,
                entity.getDelivererName(),
                entity.getDocumentNumber()
        );
    }
}