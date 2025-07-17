package com.apgroup.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemSummaryDto {
    private String name;
    private int quantity;
    private int unitPrice;
    private int totalPrice;
    private String img1;
    private Long goodsId;
}
