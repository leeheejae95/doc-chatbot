package org.chatbot.doc.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String question;

    // 대화 세션 ID 추가
    private String conversationId;
}
