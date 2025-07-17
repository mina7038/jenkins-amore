package com.apgroup.app.repository;

import java.util.List;
import java.util.Optional;

import com.apgroup.app.entity.DeliveryStatus;
import com.apgroup.app.entity.Member;
import com.apgroup.app.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apgroup.app.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(String orderId); // 카카오페이 orderId로 조회

    List<Order> findByUserId(Long userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findBySessionId(String sessionId);
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderId = :orderId")
    Optional<Order> findWithItemsByOrderId(@Param("orderId") String orderId);

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.user",
            countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> findAllWithMember(Pageable pageable);

    int countByStatusAndUser_Id(OrderStatus status, Long userId);
    int countByDeliveryStatusAndUser_Id(DeliveryStatus status, Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    List<Order> findByUserAndStatus(Member user, OrderStatus status);

    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.items i " +
            "JOIN FETCH i.goods " +
            "WHERE o.orderId = :orderId")
    Optional<Order> findByOrderIdWithItems(@Param("orderId") String orderId);
}
