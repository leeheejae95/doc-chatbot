package org.chatbot.doc.conversation.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.chatbot.doc.conversation.entity.ConversationEntity;

import java.time.LocalDateTime;

@Getter
@Builder
public class ConversationResponse {

    private String id;
    private String title;
    private int messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    public static ConversationResponse from(ConversationEntity entity) {
        return ConversationResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .messageCount(entity.getMessageCount())
                .createdAt(entity.getCreatedAt())
                .lastMessageAt(entity.getLastMessageAt())
                .build();
    }
}
