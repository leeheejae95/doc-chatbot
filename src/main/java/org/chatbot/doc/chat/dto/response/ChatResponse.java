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
    // 대화 세션 ID 추가
    private String conversationId;
}
