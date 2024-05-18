package com.capstone.learning_squad_be.dto.question;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Lob;

@Builder
@Data
public class QuestionGetReturnDto {
    Long id;

    @Lob
    String content;
}
