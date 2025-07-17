package com.apgroup.app.controller;

import com.apgroup.app.service.QnaService;
import com.apgroup.app.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/support")
public class SupportController {
    private final NoticeService noticeService;
    private final QnaService faqService;

    // 전체 목록 보기
    @GetMapping("/list")
    public String supportMain(Model model) {
        model.addAttribute("notices", noticeService.getLatest(4)); // 공지 4개
        model.addAttribute("faqs", faqService.getTopFaqs(4));      // FAQ 4개
        return "support/list";
    }

    // 1:1 문의 (채팅)
    @GetMapping("/chat")
    public String chatPage() {
        return "support/chat";
    }
}
