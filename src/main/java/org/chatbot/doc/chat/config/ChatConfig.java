package org.chatbot.doc.chat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) { // MessageWindowChatMemory : 채팅방 완성하기
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository) // InMemory에서 Jdbc로 교체
                .maxMessages(20) // 최근 20개 메시지만 보이게 설정
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory, VectorStore vectorStore) { // LLM한테 질문할 때 채팅방 내역 자동으로 챙겨주는 비서 고용
        RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.3)
                        .topK(5)
                        .vectorStore(vectorStore)
                        .build())
                .build();

        return ChatClient.builder(chatModel)
                .defaultSystem("""
                    당신은 사내 기술문서 전문 검색 도우미 입장에서 대답해줘
                    
                    반드시 다음 규칙을 지켜줘.
                    1. 반드시 한국어로만 답변해줘.
                    2. 검색된 문서 내용만 기반으로 답변해줘.
                    3. 문서에 없는 내용은 절대 추측하거나 추가하지 말아줘.
                    4. 모르면 "문서에서 관련 내용을 찾을 수 없습니다"라고 답해줘.
                    5. 수치나 코드가 있으면 정확하게 포함줘.
                    """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), ragAdvisor)
                .build();
    }
}
