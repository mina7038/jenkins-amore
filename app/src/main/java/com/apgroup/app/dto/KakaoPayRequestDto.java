package com.apgroup.app.dto;

import java.util.List;

import lombok.Data;

@Data
public class KakaoPayRequestDto {
    private String type; // "product", "cart", "cartAll"
    private Long userId;
    private Long goodsId;
    private int quantity;
    private List<Long> cartItemIds;
    private String orderId; // UUID 형태로 프론트에서 생성하거나 서버에서 만들어도 됨
}

