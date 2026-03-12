package com.ontop.wms.dto;

import lombok.Data;

@Data
public class InventoryRequest {
    private String receiptCode; // for inbound
    private String issueCode;   // for outbound
    private String status;
    private Integer warehouseId;
}