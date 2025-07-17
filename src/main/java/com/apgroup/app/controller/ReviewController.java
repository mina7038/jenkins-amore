package com.apgroup.app.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.apgroup.app.entity.OrderItem;
import com.apgroup.app.repository.ReviewRepository;
import com.apgroup.app.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.apgroup.app.entity.Review;
import com.apgroup.app.security.CustomUserDetails;
import com.apgroup.app.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final OrderService orderService;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/goods/{goodsNo}")
    public String reviewList(@PathVariable("goodsNo") Long goodsNo,
                             Model model) {
        List<Review> reviews = reviewService.getReviewsByGoodsNo(goodsNo);
        model.addAttribute("reviews", reviews);
        return "review/list"; // 리뷰 목록 템플릿
    }


    @GetMapping("/write")
    public String showWriteForm(@RequestParam("goodsNo") Long goodsNo,
                                @RequestParam("orderId") Long orderId,
                                Model model) {
        model.addAttribute("goodsNo", goodsNo);
        model.addAttribute("orderId", orderId);
        return "review/write";
    }

    private String fileUpload(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File uploadDir = new File(uploadPath);

            // ✅ 업로드 경로가 존재하지 않으면 생성
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File dest = new File(uploadDir, fileName);
            file.transferTo(dest);
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @PostMapping
    public String saveReview(@RequestParam("goodsNo") Long goodsNo,
                             @RequestParam("orderId") Long orderId,
                             @RequestParam("content") String content,
                             @RequestParam("rating") int rating,
                             @RequestParam("title") String title,
                             @RequestParam(value = "image", required = false) MultipartFile image,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getMember().getId();
        String imagePath = null;

        // 파일 저장
        if (image != null && !image.isEmpty()) {
            imagePath = fileUpload(image); // 이미지 업로드 처리 메서드 작성 필요
        }

        reviewService.createReview(memberId, goodsNo, orderId, content, rating, title, imagePath);
        return "redirect:/goods/detail/" + goodsNo + "#review";
    }

    @GetMapping("/detail/{id}")
    public String reviewDetail(@PathVariable Long id, Model model) {
        Review review = reviewService.getReviewById(id);
        model.addAttribute("review", review);
        return "review/detail";
    }


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long reviewId, Model model) {
        Review review = reviewService.getReviewById(reviewId); // 이 메서드가 goods까지 fetch해야 함
        if (review.getGoods() == null) {
            throw new IllegalStateException("연결된 상품 정보가 없습니다.");
        }

        model.addAttribute("review", review);
        model.addAttribute("goodsNo", review.getGoods().getNo()); // 템플릿에서 활용 시
        return "review/edit";
    }


    @PostMapping("/edit/{id}")
    public String updateReview(@PathVariable("id") Long reviewId,
                               @RequestParam("content") String content,
                               @RequestParam("rating") int rating,
                               @RequestParam("title") String title,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        String imagePath = (image != null && !image.isEmpty()) ? fileUpload(image) : null;

        Long memberId = userDetails.getMember().getId();
        reviewService.updateReview(reviewId, memberId, content, rating, title, imagePath);
        Long goodsNo = reviewService.getReviewById(reviewId).getGoods().getNo();
        return "redirect:/goods/detail/" + goodsNo + "#review";
    }


    @GetMapping("/delete/{id}")
    public String deleteReview(@PathVariable("id") Long reviewId,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        Long goodsNo = reviewService.getReviewById(reviewId).getGoods().getNo();
        reviewService.deleteReview(reviewId, memberId);
        return "redirect:/goods/detail/" + goodsNo + "#reviews";
    }

    @GetMapping("/all")
    public String showAllReviews(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "15") int size,
                                 Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage = reviewRepository.findAll(pageable);

        int currentPage = page;
        int totalPages = reviewPage.getTotalPages();
        int pageGroup = currentPage / 10;
        int startPage = pageGroup * 10;
        int endPage = Math.min(startPage + 9, totalPages - 1);

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "review/all";
    }

    @GetMapping("/my")
    public String showMyReviewPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long memberId = userDetails.getMember().getId();

        // 작성한 리뷰
        List<Review> myReviews = reviewService.getReviewsByMemberId(memberId);

        // 아직 작성 안 한 리뷰용 리스트: (주문 + 상품 목록 DTO)
        List<OrderItem> availableReviews = orderService.findItemsWithoutReview(memberId);

        model.addAttribute("myReviews", myReviews);
        model.addAttribute("availableReviews", availableReviews);
        return "review/my";
    }

}
