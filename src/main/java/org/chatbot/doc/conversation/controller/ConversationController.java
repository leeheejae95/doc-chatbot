package org.chatbot.doc.conversation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chatbot.doc.conversation.dto.response.ConversationResponse;
import org.chatbot.doc.conversation.service.ConversationService;
import org.chatbot.doc.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Conversation", description = "대화 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @Operation(
            summary = "대화 목록 조회",
            description = "생성된 대화 목록을 최신 메시지 순으로 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations() {
        log.info("[ConversationController] 대화 목록 조회 요청");
        List<ConversationResponse> conversations = conversationService.getConversations();
        return ResponseEntity.ok(ApiResponse.ok(conversations));
    }

    @Operation(
            summary = "대화 삭제",
            description = "대화를 삭제하면 연관된 채팅 메모리도 함께 삭제됩니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(@PathVariable String id) {
        log.info("[ConversationController] 대화 삭제 요청 - conversationId: {}", id);
        conversationService.deleteConversation(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
