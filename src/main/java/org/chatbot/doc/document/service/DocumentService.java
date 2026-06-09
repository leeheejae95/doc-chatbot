package org.chatbot.doc.document.service;

import org.chatbot.doc.document.entity.DocumentEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

public interface DocumentService {

    /**
     * PDF 문서를 업로드하고 벡터 DB에 저장
     * @param file 업로드할 PDF 파일
     * @return 저장된 청크 수
     */
    int uploadDocument(MultipartFile file);

    // 문서 목록 조회
    List<DocumentEntity> getDocuments();

    // 문서삭제
    void deleteDocument(String id);
}
