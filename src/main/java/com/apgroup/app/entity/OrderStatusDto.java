package com.apgroup.app.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusDto {
    private int depositPending;       // 입금전
    private int deliveryReady;        // 배송준비중
    private int shipping;             // 배송중
    private int delivered;            // 배송완료

}
