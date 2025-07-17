package com.apgroup.app.controller;

import com.apgroup.app.handler.ChatWebSocketHandler;
import com.apgroup.app.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Controller
@RequiredArgsConstructor

public class WebSocketController {
    private final ChatService chatService;

    @GetMapping("/chat")
    public String chat(Model model) {
        model.addAttribute("messages", chatService.getRecentMessage());
        return "support/chat";
    }
}

