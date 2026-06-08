package org.chatbot.doc.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chatbot.doc.document.service.DocumentService;
import org.chatbot.doc.global.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Document", description = "기술문서 업로드 API")
@Slf4j
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(
            summary = "PDF 문서 업로드",
            description = "PDF 파일을 업로드하면 청크 단위로 분할 후 벡터 DB에 저장됩니다."
    )
    @PostMapping(name = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadDocument(@Parameter(description = "업로드할 PDF 파일", required = true) @RequestParam("file")MultipartFile file) {
        log.info("[DocumentController] 문서 업로드 요청 파일명 : {}, 크기 : {]", file.getOriginalFilename(), file.getSize() / 1024);

        int chunkCount = documentService.uploadDocument(file);

        String message = String.format("[%s] 업로드 완료 - %d개 청크 저장", file.getOriginalFilename(), chunkCount);

        return ResponseEntity.ok(ApiResponse.ok(message));
    }
}
