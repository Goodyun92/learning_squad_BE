package com.capstone.learning_squad_be.dto.answer;

import lombok.Getter;

import javax.persistence.Lob;

@Getter
public class AnswerPostRequestDto {
    Long questionId;

    @Lob
    String userAnswer;
}
