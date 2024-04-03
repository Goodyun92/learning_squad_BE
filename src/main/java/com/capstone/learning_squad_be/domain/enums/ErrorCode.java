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
    INVALID_PASSWORD(401, HttpStatus.UNAUTHORIZED,""),

    NOT_FOUND(404, HttpStatus.NOT_FOUND, "Not Found"),
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "Forbidden"),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "Unauthorized"),
    BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "Bad Request");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}
