package org.chatbot.doc.document.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.chatbot.doc.document.entity.DocumentEntity;

import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentResponse {

    private String id;
    private String fileName;
    private String fileType;
    private int chunkCount;
    private LocalDateTime createdAt;

    public static DocumentResponse from(DocumentEntity entity) {
        return DocumentResponse.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .chunkCount(entity.getChunkCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}