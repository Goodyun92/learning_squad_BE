package com.capstone.learning_squad_be.dto.flask;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.Lob;

@Getter
@AllArgsConstructor
public class CreateQuestionReturnDto {
    @Lob
    String csv_url;
}
