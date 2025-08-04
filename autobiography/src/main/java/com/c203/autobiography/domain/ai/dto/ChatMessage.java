package com.c203.autobiography.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    /**
     * 역할: system, user, assistant 중 하나
     */
    private String role;

    /**
     * 실제 대화 내용
     */
    private String content;

    public static ChatMessage system(String content){
        return new ChatMessage("system", content);
    }

    public static ChatMessage user(String content){
        return new ChatMessage("user", content);
    }

    public static ChatMessage assistant(String content){
        return new ChatMessage("assistant", content);
    }
    public static ChatMessage of(String role, String content) {
        return ChatMessage.builder()
                .role(role)
                .content(content)
                .build();
    }
}
