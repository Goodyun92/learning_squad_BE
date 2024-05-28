package com.capstone.learning_squad_be.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    USERNAME_DUPLICATED(409, HttpStatus.CONFLICT,""),
    NICKNAME_DUPLICATED(409, HttpStatus.CONFLICT,""),
    USERNAME_NOT_FOUND(404, HttpStatus.NOT_FOUND,""),
    PASSWORD_NOT_FOUND(404, HttpStatus.NOT_FOUND,""),
    INVALID_PASSWORD(401, HttpStatus.UNAUTHORIZED,""),

    DOCUMENT_NOT_FOUND(404,HttpStatus.NOT_FOUND,""),
    QUESTION_NOT_FOUND(404,HttpStatus.NOT_FOUND,""),
    ANSWER_NOT_FOUND(404,HttpStatus.NOT_FOUND,""),
    NOT_PDF(409, HttpStatus.CONFLICT,""),
    SHORT_PDF_LENGTH(409, HttpStatus.CONFLICT,""),
    PDF_PROCESSING_ERR(409, HttpStatus.CONFLICT,""),
    CSV_PROCESSING_ERR(409, HttpStatus.CONFLICT,""),
    MODEL_SERVER_ERR(409, HttpStatus.CONFLICT,""),

    NOT_FOUND(404, HttpStatus.NOT_FOUND, "Not Found"),
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "Forbidden"),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "Unauthorized"),
    BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "Bad Request");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}
