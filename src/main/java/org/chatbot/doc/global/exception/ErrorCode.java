package org.chatbot.doc.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 오류가 발생했습니다."),

    // 문서
    FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "D001", "파일이 없습니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST,"D002", "지원하지 않는 파일 형식입니다. (PDF, Word, Excel, PPT, TXT, HWP)"),
    DUPLICATE_NOT_FOUND(HttpStatus.BAD_REQUEST, "D003","이미 업로드된 파일입니다. 삭제 후 다시 업로드 하시기 바랍니다."),
    DOCUMENT_NOT_FOUNT(HttpStatus.NOT_FOUND, "D004", "존재하지 않는 문서입니다."),
    DOCUMENT_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "D005" ,"문서 처리중 오류가 발생했습니다."),

    //챗봇
    QUESTION_REQUIRED(HttpStatus.BAD_REQUEST, "Q001", "질문을 입력해주세요."),
    DOCUMENT_NOT_UPLOADED(HttpStatus.BAD_REQUEST, "Q002", "업로드된 문서에서 관련된 내용을 찾을 수 없습니다."),
    CHAT_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Q003", "답변 생성 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

}
