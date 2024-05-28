package com.capstone.learning_squad_be.dto.oauth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokensReturnDto {
    String accessToken;
    String refreshToken;
}
