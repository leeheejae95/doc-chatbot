package org.chatbot.doc.conversation.service;

import org.chatbot.doc.conversation.dto.response.ConversationResponse;

import java.util.List;

public interface ConversationService {

    void createOrUpdate(String conversationId, String question, boolean isNew);

    List<ConversationResponse> getConversations();

    void deleteConversation(String id);
}