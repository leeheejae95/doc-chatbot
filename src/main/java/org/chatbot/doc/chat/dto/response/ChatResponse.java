package org.chatbot.doc.chat.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatResponse {

    private String answer;
    private String conversationId;
    private List<SourceDocument> sources;
}
