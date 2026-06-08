package org.chatbot.doc.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 전역 예외 처리 핸들러
 * 컨트롤러에서 발생하는 모든 예외를 공통 포맷으로 응답
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException  e) {
        log.error("[CustomException] {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.error("[MaxUploadSizeExceededException] {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail("파일 크기가 너무 큽니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[Exception] {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail("서버 오류 발생"));
    }
}
