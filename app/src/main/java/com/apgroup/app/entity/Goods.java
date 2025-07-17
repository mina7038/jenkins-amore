package com.apgroup.app.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goods {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long no;

    @Column(nullable = false)
    private String gname;

    private String comment;
    private int price1; //입고가격
    private int price2; //출고가격
    private int discount;
    private int amount;
    private String img1;
    private String img2;
    private String brand;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(nullable = false)
    @Builder.Default
    private int clickCount = 0;
    
    @OneToMany(mappedBy = "goods", cascade = CascadeType.ALL)
    private List<Review> reviews;
    

    @Builder.Default
    private LocalDateTime resdate = LocalDateTime.now();
    
    @PrePersist
    @PreUpdate
    public void calculateDiscountPrice() {
        if (discount > 0 && price1 > 0) {
            this.price2 = price1 * (100 - discount) / 100;
        } else {
            this.price2 = price1;
        }
    }
    
    
}
