package org.chatbot.doc.chat.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SourceDocument {

    private String documentId;
    private String fileName;
    private String content;
    private Double score;
}