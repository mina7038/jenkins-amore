package com.apgroup.app.service;

import com.apgroup.app.entity.Qna;
import com.apgroup.app.entity.Notice;
import com.apgroup.app.repository.QnaRepository;
import com.apgroup.app.repository.NoticeRepository;
import com.apgroup.app.repository.SupportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportService {
    private final SupportRepository supportRepository;
    private final NoticeRepository noticeRepository;
    private final QnaRepository faqRepository;

    public long count() {
        return supportRepository.count(); // JPA 기본 제공 메서드
    }

    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }

    public Notice getNoticeById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. id=" + id));
    }

    public List<Qna> getAllFaqs() {
        return faqRepository.findAll();
    }

    public Qna getFaqById(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ를 찾을 수 없습니다. id=" + id));
    }
}