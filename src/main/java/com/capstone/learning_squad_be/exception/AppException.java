package com.capstone.learning_squad_be.exception;

import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AppException extends RuntimeException{
    private ErrorCode errorCode;
    private String message;
}
