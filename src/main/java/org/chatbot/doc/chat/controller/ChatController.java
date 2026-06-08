package org.chatbot.doc.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chatbot.doc.chat.dto.request.ChatRequest;
import org.chatbot.doc.chat.dto.response.ChatResponse;
import org.chatbot.doc.chat.service.ChatService;
import org.chatbot.doc.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(
            summary = "문서 기반 질문 답변",
            description = "업로드된 문서를 기반으로 질문에 답변합니다."
    )
    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<ChatResponse>> ask(@RequestBody ChatRequest request) {
        log.info("[ChatController] 질문 요청 : {}", request.getQuestion());

        ChatResponse response = chatService.chat(request);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
