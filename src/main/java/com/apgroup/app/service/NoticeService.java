package com.apgroup.app.service;

import com.apgroup.app.entity.Notice;
import com.apgroup.app.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    public long count() {
        return noticeRepository.count(); // JPA 기본 제공 메서드
    }

    // 전체 목록 조회
    public List<Notice> getAll() {
        return noticeRepository.findAll();
    }

    public List<Notice> searchByTitle(String keyword) {
        return noticeRepository.findByTitleContainingIgnoreCase(keyword);
    }

    // ID로 단건 조회
    public Notice getById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. id=" + id));
    }

    public List<Notice> getLatest(int count) {
        return noticeRepository.findTop4ByOrderByResdateDesc();
    }

    public List<Notice> getAllDesc() {
        return noticeRepository.findAllByOrderByResdateDesc();
    }

    // 등록
    public Notice save(Notice notice) {
        notice.setResdate(LocalDateTime.now());
        return noticeRepository.save(notice);
    }

    // 수정
    public Notice update(Long id, Notice newNotice) {
        Notice origin = getById(id);
        origin.setTitle(newNotice.getTitle());
        origin.setContent(newNotice.getContent());
        return noticeRepository.save(origin);
    }

    public void delete(Long id) {
        noticeRepository.deleteById(id);
    }

    public Page<Notice> searchByTitle(String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "resdate"));
        return noticeRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    public Page<Notice> getNotices(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "resdate"));
        return noticeRepository.findAll(pageable); // 전체 공지 불러오기
    }


}
