package com.apgroup.app.controller;

import com.apgroup.app.entity.Qna;
import com.apgroup.app.entity.Member;
import com.apgroup.app.repository.MemberRepository;
import com.apgroup.app.security.CustomUserDetails;
import com.apgroup.app.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/support/qna")
@RequiredArgsConstructor
public class QnaController {
    private final QnaService qnaService;
    private final MemberRepository memberRepository;

    @GetMapping("/list")
    public String qnaList(@RequestParam(name = "category", required = false) String category,
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          Model model) {

        String username = userDetails.getUsername(); // 로그인한 사용자 아이디

        if (category != null && !category.equals("전체")) {
            model.addAttribute("qnas", qnaService.getByCategoryAndUsername(category, username));
            model.addAttribute("selectedCategory", category);
        } else {
            model.addAttribute("qnas", qnaService.getByMemberUsername(username));
            model.addAttribute("selectedCategory", "전체");
        }

        return "support/qna/list";
    }


    @GetMapping("/form")
    public String form(Model model) {
        model.addAttribute("qna", new Qna());
        return "support/qna/form";
    }

    @PostMapping("/form")
    public String submit(@ModelAttribute Qna qna,
                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            // 영속 상태의 Member로 다시 조회
            Member persistedMember = memberRepository.findById(userDetails.getMember().getId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            qna.setMember(persistedMember);
        }

        if (qna.getResdate() == null) {
            qna.setResdate(LocalDateTime.now());
        }

        qnaService.save(qna);
        return "redirect:/support/qna/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("qna", qnaService.getById(id));
        return "support/qna/detail";
    }


    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("qna", qnaService.getById(id));
        return "support/qna/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, @ModelAttribute Qna qna) {
        qnaService.update(id, qna);
        return "redirect:/support/qna/list";
    }
}
