package com.apgroup.app.service;

import com.apgroup.app.entity.Chat;
import com.apgroup.app.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

    public Chat saveChat(Chat chat) {
        chat.setTimestamp(LocalDateTime.now());
        return chatRepository.save(chat);
    }

    public List<Chat> getRecentMessage() {
        List<Chat> chat = chatRepository.findTop50ByOrderByTimestampAsc();
        return chat;
    }


}
