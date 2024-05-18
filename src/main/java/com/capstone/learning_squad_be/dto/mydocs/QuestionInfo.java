package com.capstone.learning_squad_be.dto.mydocs;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Lob;

@Builder
@Getter
public class QuestionInfo {
    Long questionId;
    Integer questionNumber;
    @Lob
    String content;
    @Lob
    String correctAnswer;
    @Lob
    String bestAnswer;
    Integer score;

}
