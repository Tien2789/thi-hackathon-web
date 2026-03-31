package com.ontop.wms.dto;

import lombok.Data;

@Data
public class InventoryRequest {
    private String receiptCode; // for inbound
    private String issueCode;   // for outbound
    private String status;
    private Integer warehouseId;
    
    // Circular 200 fields
    private String delivererName;
    private String documentNumber;
    private String receiverName;
    private String reason;

    // Signature flow
    private java.util.List<String> signerEmails;
}