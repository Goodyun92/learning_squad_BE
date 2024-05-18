package com.capstone.learning_squad_be.dto.document;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DocumentUploadReturnDto {
    Long id;

    String title;

    Integer questionSize;
}
