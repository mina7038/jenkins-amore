package com.apgroup.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 회원의 장바구니인지
    @ManyToOne(fetch = FetchType.LAZY)
    private Member user;

    // 어떤 상품인지
    @ManyToOne(fetch = FetchType.LAZY)
    private Goods goods;

    private int quantity;
}
