package com.apgroup.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apgroup.app.entity.CartItem;
import com.apgroup.app.entity.Member;

import jakarta.transaction.Transactional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    @Transactional
    void deleteByUser_Id(Long userId);
    Optional<CartItem> findByUserIdAndGoodsNo(Long userId, Long goodsNo);
    List<CartItem> findByIdInAndUser(List<Long> ids, Member user);
}
