package com.apgroup.app.repository;

import com.apgroup.app.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findTop50ByOrderByTimestampAsc();
}
