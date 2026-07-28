package org.chatbot.doc.conversation.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chatbot.doc.conversation.dto.response.ConversationResponse;
import org.chatbot.doc.conversation.entity.ConversationEntity;
import org.chatbot.doc.conversation.repository.ConversationRepository;
import org.chatbot.doc.conversation.service.ConversationService;
import org.chatbot.doc.global.exception.CustomException;
import org.chatbot.doc.global.exception.ErrorCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void createOrUpdate(String conversationId, String question, boolean isNew) {
        if (isNew) {
            String title = question.length() > 50 ? question.substring(0, 50) + "..." : question;
            conversationRepository.save(ConversationEntity.builder()
                    .id(conversationId)
                    .title(title)
                    .build());
            log.info("[ConversationService] 새 대화 생성 - conversationId: {}", conversationId);
        } else {
            conversationRepository.findById(conversationId).ifPresent(conv -> {
                conv.addMessage();
                log.info("[ConversationService] 대화 업데이트 - conversationId: {}, messageCount: {}", conversationId, conv.getMessageCount());
            });
        }
    }

    @Override
    public List<ConversationResponse> getConversations() {
        return conversationRepository.findAllByOrderByLastMessageAtDesc()
                .stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void deleteConversation(String id) {
        ConversationEntity conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CONVERSATION_NOT_FOUND));

        jdbcTemplate.update("DELETE FROM spring_ai_chat_memory WHERE conversation_id = ?", id);
        conversationRepository.delete(conversation);

        log.info("[ConversationService] 대화 삭제 완료 - conversationId: {}", id);
    }
}
