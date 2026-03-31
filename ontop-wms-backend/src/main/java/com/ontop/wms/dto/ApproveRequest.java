package com.ontop.wms.dto;

import lombok.Data;

@Data
public class ApproveRequest {
    private Integer productId;
    private Integer quantity;
}