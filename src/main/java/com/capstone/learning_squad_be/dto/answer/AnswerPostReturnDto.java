package com.capstone.learning_squad_be.dto.answer;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Lob;

@Builder
@Data
public class AnswerPostReturnDto {
    Long id;

    Integer newScore;

    Integer bestScore;

    @Lob
    String userAnswer;

    @Lob
    String correctAnswer;

    @Lob
    String bestAnswer;
}
