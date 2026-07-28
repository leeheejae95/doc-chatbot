package org.chatbot.doc.chat;

import org.chatbot.doc.chat.dto.request.ChatRequest;
import org.chatbot.doc.chat.dto.response.ChatResponse;
import org.chatbot.doc.chat.service.impl.ChatServiceImpl;
import org.chatbot.doc.conversation.service.ConversationService;
import org.chatbot.doc.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService 단위 테스트")
class ChatServiceTest {

    @InjectMocks
    private ChatServiceImpl chatService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ConversationService conversationService;

    @Test
    @DisplayName("질문 답변 성공")
    void chat_success() {
        ChatRequest request = new ChatRequest("Spring AI가 뭔가요?", null);

        given(vectorStore.similaritySearch(any(SearchRequest.class))).willReturn(List.of());
        given(chatClient.prompt()
                .user(anyString())
                .advisors(any(Consumer.class))
                .call()
                .content())
                .willReturn("Spring AI는 AI 통합 프레임워크입니다.");

        ChatResponse response = chatService.chat(request);

        assertThat(response.getAnswer()).isEqualTo("Spring AI는 AI 통합 프레임워크입니다.");
        assertThat(response.getConversationId()).isNotNull();
        assertThat(response.getSources()).isNotNull();
    }

    @Test
    @DisplayName("기존 conversationId 유지")
    void chat_existingConversationId_maintained() {
        String existingId = "existing-conversation-id";
        ChatRequest request = new ChatRequest("추가 질문입니다.", existingId);

        given(vectorStore.similaritySearch(any(SearchRequest.class))).willReturn(List.of());
        given(chatClient.prompt()
                .user(anyString())
                .advisors(any(Consumer.class))
                .call()
                .content())
                .willReturn("추가 답변입니다.");

        ChatResponse response = chatService.chat(request);

        assertThat(response.getConversationId()).isEqualTo(existingId);
    }

    @Test
    @DisplayName("빈 질문 예외")
    void chat_emptyQuestion_throwException() {
        ChatRequest request = new ChatRequest("", null);

        assertThatThrownBy(() -> chatService.chat(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("질문을 입력해주세요.");
    }

    @Test
    @DisplayName("null 질문 예외")
    void chat_nullQuestion_throwException() {
        ChatRequest request = new ChatRequest(null, null);

        assertThatThrownBy(() -> chatService.chat(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("질문을 입력해주세요.");
    }

    @Test
    @DisplayName("공백 질문 예외")
    void chat_blankQuestion_throwException() {
        ChatRequest request = new ChatRequest("   ", null);

        assertThatThrownBy(() -> chatService.chat(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("질문을 입력해주세요.");
    }
}
