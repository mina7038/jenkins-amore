package com.apgroup.app.handler;

import com.apgroup.app.entity.Chat;
import com.apgroup.app.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
        private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
        private final ChatService chatService;

        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            sessions.add(session); // 세션 시작
        }

        public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            // 메시지 전달(JSON으로 오는 메시지를 각 필드인 Sender, Message로 나누어 분리하여 저장)
            ObjectMapper mapper = new ObjectMapper();
            Chat chat = mapper.readValue(message.getPayload(), Chat.class);
            Chat saved = chatService.saveChat(chat); // DB에 저장
            String payload = mapper.writeValueAsString(saved); // 문자열로 변환
            for(WebSocketSession s : sessions) {
                s.sendMessage(new TextMessage(payload)); // 모든 대화상대에게 메시지 전달
            }
        }

        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            sessions.remove(session);
        }
    }
