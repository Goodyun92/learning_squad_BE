package com.capstone.learning_squad_be.dto.document;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.persistence.Lob;

@AllArgsConstructor
@Getter
public class DocumentUploadRequestDto {

    @Lob
    String documentUrl;

    String title;
}
