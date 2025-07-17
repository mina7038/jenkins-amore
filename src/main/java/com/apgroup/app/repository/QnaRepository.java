package com.apgroup.app.repository;

import com.apgroup.app.entity.Qna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QnaRepository extends JpaRepository<Qna, Long> {
    List<Qna> findTop4ByOrderByResdateDesc();

    List<Qna> findByMemberUsernameOrderByResdateDesc(String username);

    List<Qna> findByCategoryAndMemberUsernameOrderByResdateDesc(String category, String username);



    @Query(
            value = "SELECT q FROM Qna q LEFT JOIN q.member",
            countQuery = "SELECT COUNT(q) FROM Qna q"
    )
    Page<Qna> findAllWithMember(Pageable pageable);
}
