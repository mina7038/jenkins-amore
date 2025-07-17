package com.apgroup.app.entity;

public enum OrderStatus {
    PENDING,       // 결제 대기중
    PAID,          // 결제 완료
    CANCELLED,     // 결제 취소
    FAILED         // 결제 실패
}