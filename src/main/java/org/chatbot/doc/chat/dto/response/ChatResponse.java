package org.chatbot.doc.chat.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatResponse {

    // AI 답변
    private String answer;
    // 참조한 문서 청크 수
    private int referenceCount;
}
