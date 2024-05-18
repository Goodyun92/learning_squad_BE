package com.capstone.learning_squad_be.dto.flask;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Lob;

@Builder
@Data
public class GetSimilarityRequestDto {
    @Lob
    String sentence1;

    @Lob
    String sentence2;
}
