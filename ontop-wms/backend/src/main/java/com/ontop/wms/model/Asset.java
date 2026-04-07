package com.ontop.wms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    private Long id;

    private String name;
    private String status;
    private String description;
    private String location;
    private Date purchaseDate;
    private Double purchaseCost;
    private String supplier;

}
