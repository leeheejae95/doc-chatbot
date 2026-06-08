package org.chatbot.doc.document.service;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

    /**
     * PDF 문서를 업로드하고 벡터 DB에 저장
     * @param file 업로드할 PDF 파일
     * @return 저장된 청크 수
     */
    int uploadDocument(MultipartFile file);
}
