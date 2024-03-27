package com.capstone.learning_squad_be.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserTokenReturnDto {
    private String token;
}
