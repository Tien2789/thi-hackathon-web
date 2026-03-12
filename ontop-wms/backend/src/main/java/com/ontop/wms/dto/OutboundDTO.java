package com.ontop.wms.dto;

import java.sql.Timestamp;

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

    public static OutboundDTO fromEntity(InventoryOut entity) {
        return new OutboundDTO(
                entity.getId(),
                entity.getIssueCode(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getWarehouse() != null ? entity.getWarehouse().getName() : null
        );
    }
}