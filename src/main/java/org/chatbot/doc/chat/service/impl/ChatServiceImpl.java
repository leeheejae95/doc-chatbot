package org.chatbot.doc.chat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chatbot.doc.chat.dto.request.ChatRequest;
import org.chatbot.doc.chat.dto.response.ChatResponse;
import org.chatbot.doc.chat.dto.response.SourceDocument;
import org.chatbot.doc.chat.service.ChatService;
import org.chatbot.doc.conversation.service.ConversationService;
import org.chatbot.doc.global.exception.CustomException;
import org.chatbot.doc.global.exception.ErrorCode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ConversationService conversationService;

    @Override
    public ChatResponse chat(ChatRequest request) {
        validateRequest(request);

        boolean isNew = (request.getConversationId() == null || request.getConversationId().isBlank());
        String conversationId = isNew ? UUID.randomUUID().toString() : request.getConversationId();

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            List<SourceDocument> sources = retrieveSources(request.getQuestion());

            String answer = chatClient.prompt()
                    .user(request.getQuestion())
                    .advisors(advisor -> advisor
                            .param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();

            stopWatch.stop();
            log.info("[ChatService] 답변 생성 완료 - conversationId: {}, 출처수: {}, 응답시간: {}ms",
                    conversationId, sources.size(), stopWatch.getTotalTimeMillis());

            conversationService.createOrUpdate(conversationId, request.getQuestion(), isNew);

            return ChatResponse.builder()
                    .answer(answer)
                    .conversationId(conversationId)
                    .sources(sources)
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[ChatService] 답변 생성 실패 - 원인: {}", e.getMessage());
            throw new CustomException(ErrorCode.CHAT_PROCESSING_ERROR);
        }
    }

    @Override
    public Flux<String> stream(ChatRequest request) {
        validateRequest(request);

        String conversationId = (request.getConversationId() != null && !request.getConversationId().isBlank())
                ? request.getConversationId() : UUID.randomUUID().toString();

        return chatClient.prompt()
                .user(request.getQuestion())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    private List<SourceDocument> retrieveSources(String question) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(5)
                        .similarityThreshold(0.3)
                        .build()
        );

        return docs.stream()
                .map(doc -> SourceDocument.builder()
                        .documentId((String) doc.getMetadata().get("document_id"))
                        .fileName((String) doc.getMetadata().get("file_name"))
                        .content(truncate(doc.getText(), 200))
                        .score(doc.getScore())
                        .build())
                .toList();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    private void validateRequest(ChatRequest request) {
        if (request == null || request.getQuestion() == null
                || request.getQuestion().isEmpty() || request.getQuestion().isBlank()) {
            throw new CustomException(ErrorCode.QUESTION_REQUIRED);
        }
    }
}
