package org.chatbot.doc.document.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "documents")
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    // 파일명
    @Column(name = "file_name", nullable = false)
    private String fileName;

    // 파일 타입
    @Column(name = "file_type", nullable = false)
    private String fileType;

    // 저장된 청크수
    @Column(name = "chunk_count", nullable = false)
    private int chunkCount;

    // 생성일자
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public DocumentEntity(String fileName, String fileType, int chunkCount) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.chunkCount = chunkCount;
        this.createdAt = LocalDateTime.now();
    }
}
