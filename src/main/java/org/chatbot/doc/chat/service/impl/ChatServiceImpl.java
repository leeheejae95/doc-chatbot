package org.chatbot.doc.chat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chatbot.doc.chat.dto.request.ChatRequest;
import org.chatbot.doc.chat.dto.response.ChatResponse;
import org.chatbot.doc.chat.service.ChatService;
import org.chatbot.doc.global.exception.CustomException;
import org.chatbot.doc.global.exception.ErrorCode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;

    @Override
    public ChatResponse chat(ChatRequest request) {
        validateRequest(request);

        String conversationId = (request.getConversationId() != null && !request.getConversationId().isBlank())
                ? request.getConversationId()
                : UUID.randomUUID().toString();

        try{
            // 답변 생성 (LLM)
            String answer = chatClient.prompt()
                    .user(request.getQuestion())
                    .advisors(advisor -> advisor
                            .param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();

            log.info("[ChatService] 답변 생성 완료 - conversationId : {}", conversationId);

            return ChatResponse.builder()
                    .answer(answer)
                    .conversationId(conversationId)
                    .build();

        } catch (Exception e) {
            log.error("[ChatService] 답변 생성 실패 - 원인: {}", e.getMessage());
            throw new CustomException(ErrorCode.CHAT_PROCESSING_ERROR);
        }
    }

    @Override
    public Flux<String> stream(ChatRequest request) { // webflux는 생성즉시 답변가능
        validateRequest(request);

        String conversationId = (request.getConversationId() != null && !request.getConversationId().isBlank())
                ? request.getConversationId() : UUID.randomUUID().toString();

        return chatClient.prompt()
                .user(request.getQuestion())
                .advisors(advisor-> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    private void validateRequest (ChatRequest request) {
        if(request == null || request.getQuestion().isEmpty() || request.getQuestion().isBlank()) {
            throw new CustomException(ErrorCode.QUESTION_REQUIRED);
        }
    }
}
