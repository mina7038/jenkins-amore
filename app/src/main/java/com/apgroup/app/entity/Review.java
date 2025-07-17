package com.apgroup.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating;  // 별점 (1~5)


    @Column(length = 1000)
    private String content;  // 리뷰 내용

    private String imagePath; // 리뷰 이미지 경로 (선택)

    private LocalDateTime createdAt;

    @Column(length = 200)
    private String title;

    // ✅ 작성자 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true) // ✅ 탈퇴 시 null 허용
    private Member member;

    // ✅ 상품 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id")
    private Goods goods;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // ✅ 이게 꼭 있어야 함
    private Order order;
}
