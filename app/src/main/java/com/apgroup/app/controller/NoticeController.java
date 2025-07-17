package com.apgroup.app.controller;

import com.apgroup.app.service.NoticeService;
import com.apgroup.app.entity.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/support/notice")
public class NoticeController {

    private final NoticeService noticeService;

    // 최신순 공지사항 목록(내림차순)
    @GetMapping("/list")
    public String noticeList(@RequestParam(required = false) String keyword, Model model) {
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("notices", noticeService.searchByTitle(keyword));
        } else {
            model.addAttribute("notices", noticeService.getAllDesc());
        }
        model.addAttribute("keyword", keyword); // 검색어 유지
        return "support/notice/list";
    }

    // 공지사항 등록 폼
    @GetMapping("/form")
    public String form(Model model) {
        model.addAttribute("notice", new Notice());
        return "support/notice/form";
    }

    // 공지사항 등록 처리
    @PostMapping("/form")
    public String submit(@ModelAttribute Notice notice) {
        if (notice.getResdate() == null) {
            notice.setResdate(LocalDateTime.now());
        }
        noticeService.save(notice);
        return "redirect:/support/notice/list";
    }

    // 공지사항 상세
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("notice", noticeService.getById(id));
        return "support/notice/detail";
    }

    // 공지사항 수정 폼
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("notice", noticeService.getById(id));
        return "support/notice/edit";
    }

    // 공지사항 수정 처리
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, @ModelAttribute Notice notice) {
        noticeService.update(id, notice);
        return "redirect:/support/notice/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        noticeService.delete(id);
        return "redirect:/support/notice/list";
    }
}
