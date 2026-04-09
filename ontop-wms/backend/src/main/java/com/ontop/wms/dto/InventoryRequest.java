package com.ontop.wms.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {
    private String receiptCode; // for inbound
    private String issueCode;   // for outbound
    private String status;
    private Integer warehouseId; // Changed to Integer
    
    // Circular 200 Fields
    private String delivererName;
    private String receiverName;
    private String reason;
    private String documentNumber;
    private String location;
    
    private List<ApproveRequest> details;
    private List<String> signerEmails;
}