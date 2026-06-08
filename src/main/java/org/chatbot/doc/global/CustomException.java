package org.chatbot.doc.global;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 예외 처리를 위한 커스텀 예외
 * ErrorCode를 통해 예외 유형을 구분
 */
@Getter
public class CustomException extends RuntimeException{

    private final HttpStatus status;
    private final String code;

    public CustomException(HttpStatus status, String code, String messge) {
        super(messge);
        this.status = status;
        this.code = code;
    }

    public static CustomException notFound(String message) {
        return new CustomException(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }

    public static CustomException badRequest(String message) {
        return new CustomException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }

    public static CustomException internalError(String message) {
        return new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",message);
    }
}
