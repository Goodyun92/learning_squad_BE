package com.capstone.learning_squad_be.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserJoinRequestDto {
    private String userName;
    private String password;
    private String nickName;
}
