package org.chatbot.doc.chat.service;

import org.chatbot.doc.chat.dto.request.ChatRequest;
import org.chatbot.doc.chat.dto.response.ChatResponse;
import reactor.core.publisher.Flux;

public interface ChatService {
    /**
     * 질문을 받아 RAG 기반으로 답변 생성
     * @param request 사용자 질문
     * @return AI 답변 + 참조 문서 수
     */
    ChatResponse chat(ChatRequest request);

    // 스트리밍 응답 추가
    Flux<String> stream(ChatRequest request);
}
