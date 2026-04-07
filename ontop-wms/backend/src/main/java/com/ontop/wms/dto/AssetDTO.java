package com.ontop.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetDTO {
    private Long id;
    private String assetCode;
    private String name;
    private String description;
    private Integer warehouseId;
    private String warehouseName;
    private Integer quantity;
    private LocalDateTime createdAt;
}
