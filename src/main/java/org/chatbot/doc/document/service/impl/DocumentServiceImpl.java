package org.chatbot.doc.document.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chatbot.doc.document.service.DocumentService;
import org.chatbot.doc.global.exception.CustomException;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final VectorStore vectorStore;

    @Override
    public int uploadDocument(MultipartFile file) {
        validDataFile(file);
        // TODO 파일 확장 추가 (TikaDocumentReader)
        try{
            Resource resource = file.getResource();
            PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
            List<Document> documents = reader.read();

            log.info("[DocumentService] PDF 파싱 완료 - 파일명 : {}, 페이지수 : {}",file.getOriginalFilename(), documents.size());

            TokenTextSplitter splitter = TokenTextSplitter.builder()
                    // 청크 하나의 최대 토큰 수
                    // 512토큰 = 대략 영어 400단어, 한글 200~300자
                    .withChunkSize(512)
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
            List<Document> chunk = splitter.apply(documents);

            log.info("[DocumentService] chunk 사이즈 : {}",chunk.size());

            vectorStore.add(chunk); // 백터 DB에 저장

            log.info("[DocumentService] .백터 저장 완료 - 파일명 {}", file.getOriginalFilename());

            return chunk.size();
        } catch(Exception e) {
            log.error("[DocumentService] 문서 업로드 실패 - 파일명 : {}, 원인 : {}", file.getOriginalFilename(), e.getMessage());
            throw CustomException.internalError("문서 처리중 오류 발생");
        }
    }

    private void validDataFile(MultipartFile file) {
        if(file == null || file.isEmpty()) {
            throw CustomException.badRequest("파일이 없습니다.");
        }

        String originFileName = file.getOriginalFilename();
        if(originFileName == null || !originFileName.toLowerCase().endsWith(".pdf")) {
            throw CustomException.badRequest("PDF파일만 업로드 가능합니다.");
        }
    }
}
