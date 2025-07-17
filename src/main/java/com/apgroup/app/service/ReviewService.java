package com.apgroup.app.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apgroup.app.entity.Order;
import com.apgroup.app.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apgroup.app.entity.Goods;
import com.apgroup.app.entity.Member;
import com.apgroup.app.entity.Review;
import com.apgroup.app.repository.GoodsRepository;
import com.apgroup.app.repository.MemberRepository;
import com.apgroup.app.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final GoodsRepository goodsRepository;
    private final OrderRepository orderRepository;

    public long count() {
        return reviewRepository.count(); // JPA 기본 제공 메서드
    }

    public Page<Review> findAllWithGoods(Pageable pageable) {
        return reviewRepository.findAllWithGoods(pageable);
    }

    // ✅ 특정 상품에 대한 리뷰 목록 조회
    public List<Review> getReviewsByGoodsNo(Long goodsNo) {
        return reviewRepository.findByGoods_NoOrderByCreatedAtDesc(goodsNo);
    }

    // ✅ 리뷰 작성
    @Transactional
    public void createReview(Long memberId, Long goodsId, Long orderId, String content, int rating, String title, String imagePath) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        Goods goods = goodsRepository.findById(goodsId).orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (reviewRepository.existsByMemberAndGoodsAndOrder(member, goods, order)) {
            throw new IllegalStateException("이미 해당 주문에 대해 리뷰를 작성했습니다.");
        }

        Review review = Review.builder()
                .member(member)
                .goods(goods)
                .order(order)
                .content(content)
                .rating(rating)
                .title(title)
                .imagePath(imagePath)
                .createdAt(LocalDateTime.now())
                .build();

        reviewRepository.save(review);
    }

    // ✅ 리뷰 삭제 (작성자 본인만)
    @Transactional
    public void deleteReview(Long reviewId, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        boolean isOwner = review.getMember() != null && review.getMember().getId().equals(memberId);
        boolean isAdmin = member.getRole().equals("ROLE_ADMIN");

        if (!isOwner && !isAdmin) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        reviewRepository.delete(review);
    }

    // ✅ 리뷰 수정
    @Transactional
    public void updateReview(Long reviewId, Long memberId, String newContent, int newRating, String newTitle, String newImagePath) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();

        if (review.getMember() == null || !review.getMember().getId().equals(memberId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        review.setContent(newContent);
        review.setRating(newRating);
        review.setTitle(newTitle);
        review.setImagePath(newImagePath);
    }

    // ✅ 회원 탈퇴 시 작성자만 null 처리
    @Transactional
    public void nullifyReviewsByMember(Long memberId) {
        List<Review> reviews = reviewRepository.findByMember_Id(memberId);
        for (Review review : reviews) {
            review.setMember(null);
        }
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findWithGoodsById(id)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
    }
    
    public double calculateAverageScore(Long goodsNo) {
        List<Review> reviews = reviewRepository.findByGoods_No(goodsNo); // ✅ 수정
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

    public int countReviews(Long goodsNo) {
        return reviewRepository.countByGoods_No(goodsNo); // ✅ 이건 개수니까 맞음
    }

    public int calculateSatisfaction(Long goodsNo) {
        List<Review> reviews = reviewRepository.findByGoods_No(goodsNo); // ✅ 수정
        long positive = reviews.stream().filter(r -> r.getRating() >= 4).count();
        return reviews.isEmpty() ? 0 : (int) ((positive * 100.0) / reviews.size());
    }

    public Map<Integer, Integer> getScoreCountMap(Long goodsNo) {
        List<Review> reviews = reviewRepository.findByGoods_No(goodsNo); // ✅ 수정
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 1; i <= 5; i++) map.put(i, 0);
        for (Review r : reviews) {
            map.put(r.getRating(), map.getOrDefault(r.getRating(), 0) + 1);
        }
        return map;
    }
    
    public Map<Integer, Integer> getScorePercentMap(Long goodsNo) {
        Map<Integer, Integer> countMap = getScoreCountMap(goodsNo);
        int total = countMap.values().stream().mapToInt(Integer::intValue).sum();

        Map<Integer, Integer> percentMap = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            int count = countMap.getOrDefault(i, 0); // ✅ 기본값 0 설정
            int percent = total == 0 ? 0 : (int) ((count * 100.0) / total);
            percentMap.put(i, percent);
        }
        
        return percentMap;
        
    }

    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    public List<Review> getReviewsByMemberId(Long memberId) {
        return reviewRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }




}
