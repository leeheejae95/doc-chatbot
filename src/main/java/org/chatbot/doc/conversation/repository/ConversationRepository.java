package org.chatbot.doc.conversation.repository;

import org.chatbot.doc.conversation.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<ConversationEntity, String> {

    List<ConversationEntity> findAllByOrderByLastMessageAtDesc();
}