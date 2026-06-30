package org.chatbot.doc.document.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chatbot.doc.document.entity.DocumentEntity;
import org.chatbot.doc.document.repository.DocumentRepository;
import org.chatbot.doc.document.service.DocumentService;
import org.chatbot.doc.global.exception.CustomException;
import org.chatbot.doc.global.exception.ErrorCode;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public int uploadDocument(MultipartFile file) {
        validDataFile(file);

        String fileName = file.getOriginalFilename();
        if(documentRepository.findByFileName(fileName).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_NOT_FOUND);
        }

        try{
            Resource resource = file.getResource();
            List<Document> documents = getDocumentReader(file.getOriginalFilename(), resource).read();

            log.info("[DocumentService] PDF 파싱 완료 - 파일명 : {}, 페이지수 : {}",file.getOriginalFilename(), documents.size());

            TokenTextSplitter splitter = TokenTextSplitter.builder()
                    // 청크 하나의 최대 토큰 수
                    // 512토큰 = 대략 영어 400단어, 한글 200~300자
                    .withChunkSize(256)
                    // 청크의 최소 문자 수
                    // 128자 미만이면 너무 짧아서 의미없음 -> 앞 청크랑 합치기
                    .withMinChunkSizeChars(128)
                    // 임베딩할 최소 청크 길이
                    // 5자 미만은 벡터 저장 건너뜀
                    .withMinChunkLengthToEmbed(5)
                    // 하나의 문서에서 만들 수 있는 최대 청크 수
                    // 10000개 초과하면 잘라버림 (무한 루프 방지)
                    .withMaxNumChunks(10000)
                    // 청크 분할 시 구분자(줄바꿈, 마침표 등) 유지 여부
                    // true = 구분자 포함해서 저장 (문맥 유지에 유리)
                    .withKeepSeparator(true)
                    .build();
            List<Document> chunks = splitter.apply(documents);

            log.info("[DocumentService] chunk 사이즈 : {}",chunks.size());

            String fileType = getFileExtension(file.getOriginalFilename());
            DocumentEntity documentEntity = documentRepository.save(DocumentEntity.builder()
                    .fileName(file.getOriginalFilename())
                    .fileType(fileType)
                    .chunkCount(chunks.size())
                    .build());
            chunks.forEach(chunk -> chunk.getMetadata().put("document_id", documentEntity.getId()));

            log.info("[DocumentService] .백터 저장 완료 - 파일명 {}", file.getOriginalFilename());

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            vectorStore.add(chunks); // 백터 DB에 저장
            stopWatch.stop();

            log.info("[DocumentService] 임베딩 저장 완료 - 파일명: {}, 청크수: {}, 저장시간: {}ms",
                    file.getOriginalFilename(), chunks.size(), stopWatch.getTotalTimeMillis());

            return chunks.size();
        } catch(Exception e) {
            log.error("[DocumentService] 문서 업로드 실패 - 파일명 : {}, 원인 : {}", file.getOriginalFilename(), e.getMessage());
            throw new CustomException(ErrorCode.DOCUMENT_PROCESSING_ERROR);
        }
    }

    @Override
    public List<DocumentEntity> getDocuments() {
        return documentRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public void deleteDocument(String id) {
        DocumentEntity documentEntity = documentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_NOT_FOUNT));

        jdbcTemplate.update("DELETE FROM vector_store WHERE metadata->>'document_id' = ?", id);

        documentRepository.delete(documentEntity);


        log.info("[DocumentServiceImpl] 문서 삭제 완료 - 파일명 : {}", documentEntity.getFileName());
    }

    private DocumentReader getDocumentReader(String fileName, Resource resource) {
        if(fileName.toLowerCase().endsWith(".pdf")) {
            return new PagePdfDocumentReader(resource);
        }

        return new TikaDocumentReader(resource);
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".")+1);
    }

    private void validDataFile(MultipartFile file) {
        if(file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        String filename = file.getOriginalFilename();
        if (!filename.endsWith(".pdf") &&
                !filename.endsWith(".docx") &&
                !filename.endsWith(".doc") &&
                !filename.endsWith(".xlsx") &&
                !filename.endsWith(".xls") &&
                !filename.endsWith(".pptx") &&
                !filename.endsWith(".txt") &&
                !filename.endsWith(".hwp")) {
            throw new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }
    }
}
