package org.chatbot.doc.document.repository;

import org.chatbot.doc.document.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    // 파일명으로 중복 체크
    Optional<DocumentEntity> findByFileName(String fileName);

    // 최신순으로 목록 조회
    List<DocumentEntity> findAllByOrderByCreatedAtDesc();
}
