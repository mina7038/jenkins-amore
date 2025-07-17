package com.apgroup.app.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Long id;               // 장바구니 항목 ID
    private Long goodsNo;          // 상품 ID
    private String gname;          // 상품명
    private String img1;           // 상품 대표 이미지
    private int price;             // 판매가
    private int quantity;          // 수량
    private int totalPrice;        // 합계
}
