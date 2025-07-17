package com.apgroup.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 정보 (회원 또는 비회원 구분 가능하게)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Member user;

    private String sessionId; // 비회원일 경우 식별용

    private String orderName;    // 주문 제목 (예: "상품명 외 2건")
    private int totalAmount;     // 총 결제 금액

    private String paymentKey;   // 카카오페이 결제 키
    private String orderId;      // 카카오페이 orderId (UUID 등)
    private int quantity;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime paidAt;

    private String recipientName;
    private String phone;
    private String postcode;
    private String address1;
    private String address2;

    private int shippingFee;
    private int finalAmount;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;
    
    @ElementCollection
    private List<Long> cartItemIds;
    
    @Transient
    public int getTotalQuantity() {
        return items.stream()
                    .mapToInt(OrderItem::getQuantity)
                    .sum();
    }

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
    
}
