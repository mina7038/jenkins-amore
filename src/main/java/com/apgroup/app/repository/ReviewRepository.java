package com.apgroup.app.repository;

import com.apgroup.app.entity.Goods;
import com.apgroup.app.entity.Member;
import com.apgroup.app.entity.Order;
import com.apgroup.app.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"goods"})
    Optional<Review> findWithGoodsById(Long id);

    // 특정 상품의 리뷰 목록 (최신순)
    List<Review> findByGoods_NoOrderByCreatedAtDesc(Long goodsNo);

    // 특정 회원이 작성한 리뷰 목록
    List<Review> findByMember_Id(Long memberId);

    // 특정 상품의 리뷰 수
    int countByGoods_No(Long goodsNo);
    
    List<Review> findByGoods_No(Long goodsNo);

    List<Review> findAllByOrderByCreatedAtDesc();

    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    Page<Review> findAllWithGoods(Pageable pageable);

    List<Review> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    boolean existsByMemberAndGoods(Member member, Goods goods);
    boolean existsByMemberAndGoodsAndOrder(Member member, Goods goods, Order order);

}


