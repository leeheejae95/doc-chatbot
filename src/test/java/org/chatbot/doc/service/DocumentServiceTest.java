package org.chatbot.doc.service;

import org.chatbot.doc.document.entity.DocumentEntity;
import org.chatbot.doc.document.repository.DocumentRepository;
import org.chatbot.doc.document.service.impl.DocumentServiceImpl;
import org.chatbot.doc.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentService 단위 테스트")
public class DocumentServiceTest {

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Mock
    private VectorStore vectorStore;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private MultipartFile multipartFile;

    @Test
    @DisplayName("문서 목록 조회 - 성공")
    void getDocuments_success() {
        List<DocumentEntity> documentEntityList = List.of(
                DocumentEntity.builder()
                        .fileName("EAI_스펙.pdf")
                        .fileType("pdf")
                        .chunkCount(3)
                        .build(),
                DocumentEntity.builder()
                        .fileName("test.ppt")
                        .fileType("ppt")
                        .chunkCount(3)
                        .build()
        );

        given(documentRepository.findAllByOrderByCreatedAtDesc()).willReturn(documentEntityList);

        List<DocumentEntity> result = documentService.getDocuments();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFileName()).isEqualTo("EAI_스펙.pdf");
        verify(documentRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("문서 업로드 - 파일 없음 예외")
    void uploadDocument_fileEmpty_throwException() {
        given(multipartFile.isEmpty()).willReturn(true);

        assertThatThrownBy(()-> documentService.uploadDocument(multipartFile))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("파일이 없습니다.");
    }

    @Test
    @DisplayName("문서 업로드 - 지원하지 않는 파일 형식 예외")
    void uploadDocument_unsupportedFileType_thrownException() {
        given(multipartFile.isEmpty()).willReturn(false);
        given(multipartFile.getOriginalFilename()).willReturn("image.img");

        assertThatThrownBy(()->documentService.uploadDocument(multipartFile))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("지원하지 않는 파일 형식입니다.");
    }

    @Test
    @DisplayName("문서 업로드 - 중복파일 예외")
    void uploadDocument_duplicateFile_throwException() {
        given(multipartFile.isEmpty()).willReturn(false); // 파일이 있음
        given(multipartFile.getOriginalFilename()).willReturn("test.pdf");
        given(documentRepository.findByFileName("test.pdf")) // 해당 파일이름으로 문서 엔티티에 찾음
                .willReturn(Optional.of(DocumentEntity.builder()
                        .fileName("test.pdf")
                        .fileType("pdf")
                        .chunkCount(3)
                        .build())
                );
        assertThatThrownBy(()->documentService.uploadDocument(multipartFile))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 업로드된 파일입니다.");

    }

    @Test
    @DisplayName("문서 삭제 예외")
    void deleteDocument_uploadDocument_duplicateFile_throwException() {
        given(documentRepository.findById("test1")).willReturn(Optional.empty());

        assertThatThrownBy(()->documentService.deleteDocument("test1"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("존재하지 않는 문서입니다.");
    }

    @Test
    @DisplayName("문서 삭제 성공")
    void document_delete_success() {
        DocumentEntity documentEntity = DocumentEntity.builder()
                .fileName("test.pdf")
                .fileType("pdf")
                .chunkCount(5)
                .build();
        given(documentRepository.findById("testId")).willReturn(Optional.of(documentEntity));

        documentService.deleteDocument("testId");

        verify(jdbcTemplate, times(1)).update(any(), eq("testId"));
        verify(documentRepository, times(1)).delete(documentEntity);

    }

}
