package com.apgroup.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private boolean hasReview = false;

    @ManyToOne
    @JoinColumn(name = "goods_id")
    private Goods goods;

    private int quantity;
    private int unitPrice;
    private int totalPrice;
}
