package org.chatbot.doc.chat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chatbot.doc.chat.dto.request.ChatRequest;
import org.chatbot.doc.chat.dto.response.ChatResponse;
import org.chatbot.doc.chat.service.ChatService;
import org.chatbot.doc.global.exception.CustomException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    private static final int TOP_K = 3;
    private static final double SIMILARITY_THRESHOLD = 0.3;

    @Override
    public ChatResponse chat(ChatRequest request) {

        validateRequest(request);

        try{
            // 1. 질문과 유사한 문서 청크 검색 (RAG)
            List<Document> references = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(request.getQuestion())
                            .topK(TOP_K)
                            .similarityThreshold(SIMILARITY_THRESHOLD)
                            .build()
            );

            log.info("[ChatService] 유사 문서 검색 완료 - 질문 : {}, 검색된 청크 수 : {}", request.getQuestion(), references.size());

            if (references.isEmpty()) {
                return ChatResponse.builder()
                        .answer("업로드된 문서에서 관련 내용을 찾을 수 없습니다. 문서를 먼저 업로드해주세요.")
                        .referenceCount(0)
                        .build();
            }

            // 2. 검색된 청크를 컨텍스트로 조합
            String context = references.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n---\n\n"));

            // 3. 프롬프트 구성 후 LLM호출
            String prompt = buildPrompt(request.getQuestion(), context);
            // 답변 생성 (LLM)
            String answer = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("[ChatService] 답변 생성 완료 - 참조 청크 수 : {}", references.size());

            return ChatResponse.builder()
                    .answer(answer)
                    .referenceCount(references.size())
                    .build();


        } catch (Exception e) {
            log.error("[ChatService] 답변 생성 실패 - 원인: {}", e.getMessage());
            throw CustomException.internalError("답변 생성 중 오류가 발생했습니다.");
        }
    }

    private String buildPrompt(String question, String context) {
        return String.format("""
                당신은 사내 기술문서 전문 검색 도우미입니다.
                아래 제공된 문서 내용을 기반으로 질문에 답변해주세요.
                문서에 없는 내용은 "문서에서 찾을 수 없습니다"라고 답변하세요.
                
                [참조 문서]
                %s
                
                [질문]
                %s
                
                [답변]
                """, context, question);
    }

    private void validateRequest (ChatRequest request) {
        if(request == null || request.getQuestion().isEmpty() || request.getQuestion().isBlank()) {
            throw CustomException.badRequest("질문을 입력해주세요.");
        }
    }
}
