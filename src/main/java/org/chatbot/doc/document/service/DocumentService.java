package org.chatbot.doc.document.service;

import org.chatbot.doc.document.dto.response.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    int uploadDocument(MultipartFile file);

    List<DocumentResponse> getDocuments();

    void deleteDocument(String id);
}
