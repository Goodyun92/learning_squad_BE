package com.capstone.learning_squad_be.dto.mydocs;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MydocsReturnDto {
    List<DocumentInfo> documents;
}
