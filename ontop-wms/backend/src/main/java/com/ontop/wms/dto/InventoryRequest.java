package com.ontop.wms.dto;

import java.util.List;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {
    private String receiptCode; // for inbound
    private String issueCode;   // for outbound
    private String status;
    private Integer warehouseId;
    private List<String> signerEmails;

    public String getReceiptCode() {
        return receiptCode;
    }

    public void setReceiptCode(String receiptCode) {
        this.receiptCode = receiptCode;
    }

    public String getIssueCode() {
        return issueCode;
    }

    public void setIssueCode(String issueCode) {
        this.issueCode = issueCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Integer warehouseId) {
        this.warehouseId = warehouseId;
    }

    public List<String> getSignerEmails() {
        return signerEmails;
    }

    public void setSignerEmails(List<String> signerEmails) {
        this.signerEmails = signerEmails;
    }
}