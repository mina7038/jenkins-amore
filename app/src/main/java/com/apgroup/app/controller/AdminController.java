package com.apgroup.app.controller;

import com.apgroup.app.entity.*;
import com.apgroup.app.repository.QnaRepository;
import com.apgroup.app.repository.GoodsRepository;
import com.apgroup.app.repository.NoticeRepository;
import com.apgroup.app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final MemberService memberService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final QnaService qnaService;
    private final QnaRepository qnaRepository;
    private final GoodsService goodsService;
    private final GoodsRepository goodsRepository;
    private final NoticeService noticeService;
    private final NoticeRepository noticeRepository;

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("totalMembers", memberService.count());
        model.addAttribute("totalOrders", orderService.count());
        model.addAttribute("totalReviews", reviewService.count());
        model.addAttribute("totalQna", qnaService.count());
        model.addAttribute("totalGoods", goodsService.count());
        model.addAttribute("totalNotices", noticeService.count());
        return "admin/dashboard";
    }

    @GetMapping("/admin/notices")
    public String adminNoticeList(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(required = false) String keyword,
                                  Model model) {
        Page<Notice> noticePage;
        if (keyword != null && !keyword.isBlank()) {
            noticePage = noticeService.searchByTitle(keyword, page);
            model.addAttribute("keyword", keyword);
        } else {
            noticePage = noticeService.getNotices(page);
        }

        model.addAttribute("noticePage", noticePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", noticePage.getTotalPages());
        return "admin/notices";
    }

    @GetMapping("/admin/members")
    public String adminMembers(@RequestParam(defaultValue = "0") int page,
                               Model model) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").descending());
        Page<Member> memberPage = memberService.findAll(pageable);

        model.addAttribute("memberPage", memberPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", memberPage.getTotalPages());

        return "admin/members";
    }

    @GetMapping("/admin/goods")
    public String adminGoodsList(@RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 Model model) {
        Page<Goods> goodsPage;

        if (keyword != null && !keyword.isBlank()) {
            goodsPage = goodsService.searchByName(keyword, page);
        } else {
            goodsPage = goodsService.getAll(page);
        }

        model.addAttribute("goodsPage", goodsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", goodsPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        return "admin/goods"; // ← 이 템플릿 경로에 상품 테이블이 있어야 함
    }

    @GetMapping("/admin/orders")
    public String adminOrders(@RequestParam(defaultValue = "0") int page,
                              Model model) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Order> orderPage = orderService.findAllWithMember(pageable);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());

        return "admin/orders";
    }

    @GetMapping("/admin/reviews")
    public String adminReviews(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Review> reviewPage = reviewService.findAllWithGoods(pageable);

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());

        return "admin/reviews";
    }

    @GetMapping("/admin/qna")
    public String adminQnaList(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("resdate").descending());

        Page<Qna> qnaPage = qnaService.findAllWithMember(pageable); // ✅ 페이지네이션 적용된 전체 목록

        model.addAttribute("qnaPage", qnaPage);
        model.addAttribute("qna", qnaPage.getContent()); // 실제 목록
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", qnaPage.getTotalPages());

        return "admin/qna";
    }

    @PostMapping("/support/qna/{id}/answer")
    public String saveAnswer(@PathVariable("id") Long id,
                             @RequestParam("answer") String answer) {
        Qna qna = qnaService.getById(id);
        qna.setAnswer(answer);
        qna.setAnswerDate(LocalDateTime.now());
        qnaService.save(qna);
        return "redirect:/support/qna/" + id;
    }

    @PostMapping("/support/qna/{id}/delete-answer")
    public String deleteAnswer(@PathVariable Long id) {
        Qna qna = qnaService.getById(id);
        qna.setAnswer(null);
        qna.setAnswerDate(null);
        qnaService.save(qna);
        return "redirect:/support/qna/" + id;
    }
}
