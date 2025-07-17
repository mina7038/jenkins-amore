package com.apgroup.app.repository;

import com.apgroup.app.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findTop4ByOrderByResdateDesc();
    List<Notice> findByTitleContainingIgnoreCase(String keyword);
    List<Notice> findAllByOrderByResdateDesc();
    Page<Notice> findAll(Pageable pageable);
    Page<Notice> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
