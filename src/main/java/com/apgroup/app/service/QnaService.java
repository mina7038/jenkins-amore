package com.apgroup.app.service;

import com.apgroup.app.entity.Qna;
import com.apgroup.app.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaService {
    private final QnaRepository qnaRepository;

    public long count() {
        return qnaRepository.count(); // JPA 기본 제공 메서드
    }

    //관리자 거
    public Page<Qna> findAllWithMember(Pageable pageable) {
        return qnaRepository.findAllWithMember(pageable);
    }

    public Qna getById(Long id) {
        return qnaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Q&A를 찾을 수 없습니다. id=" + id));
    }

    public Qna save(Qna faq) {
        faq.setResdate(LocalDateTime.now());
        return qnaRepository.save(faq);
    }

    public Qna answerToFaq(Long id, String answer) {
        Qna faq = getById(id);
        faq.setAnswer(answer);
        return qnaRepository.save(faq);
    }

    public Qna update(Long id, Qna newqna) {
        Qna origin = getById(id);
        origin.setCategory(newqna.getCategory());
        origin.setQuestion(newqna.getQuestion());
        origin.setAnswer(newqna.getAnswer());
        return qnaRepository.save(origin);
    }

    public List<Qna> getTopFaqs(int count) {
        return qnaRepository.findTop4ByOrderByResdateDesc();
    }

    public List<Qna> getByMemberUsername(String username) {
        return qnaRepository.findByMemberUsernameOrderByResdateDesc(username);
    }

    public List<Qna> getByCategoryAndUsername(String category, String username) {
        return qnaRepository.findByCategoryAndMemberUsernameOrderByResdateDesc(category, username);
    }

}
