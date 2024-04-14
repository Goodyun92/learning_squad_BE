package com.capstone.learning_squad_be.service;

import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import com.capstone.learning_squad_be.domain.user.User;
import com.capstone.learning_squad_be.dto.user.UserJoinRequestDto;
import com.capstone.learning_squad_be.exception.AppException;
import com.capstone.learning_squad_be.jwt.JwtService;
import com.capstone.learning_squad_be.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.capstone.learning_squad_be.domain.enums.Role.ROLE_USER;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwtService;

    @Transactional
    public void join(UserJoinRequestDto dto){

        String userName = dto.getUserName();
        String password = dto.getPassword();
        String nickName = dto.getNickName();

        // userName 중복 check
        userRepository.findByUserName(userName)
                .ifPresent(user -> {
                            throw new AppException(ErrorCode.USERNAME_DUPLICATED, userName + "는 이미 존재합니다.");
                        }
                );

        // nickName 중복 check
        userRepository.findByNickName(nickName)
                .ifPresent(user -> {
                            throw new AppException(ErrorCode.NICKNAME_DUPLICATED, nickName + "는 이미 존재합니다.");
                        }
                );

        // 유저 저장
        User user = User.builder()
                .userName(userName)
                .password(encoder.encode(password))
                .nickName(nickName)
                .role(ROLE_USER)
                .build();
        userRepository.save(user);

    }

    public String login(String userName, String password){

        //userName 없음
        User selectedUser = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.USERNAME_NOT_FOUND, userName + "이 없습니다."));

        //password 틀림
        if(!encoder.matches(password, selectedUser.getPassword())){
            throw new AppException(ErrorCode.INVALID_PASSWORD, "패스워드를 잘못 입력했습니다.");
        }

        //access token 발행
        return jwtService.getAccessToken(selectedUser.getUserName());
    }

    public User updateNickName(String userName, String nickName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND, "사용자"+ userName + "이 없습니다."));

        // nickName 중복 check
        userRepository.findByNickName(nickName)
                .ifPresent(thatuser -> {
                            throw new AppException(ErrorCode.NICKNAME_DUPLICATED, nickName + "는 이미 존재합니다.");
                        }
                );


        user.setNickName(nickName);
        userRepository.save(user);
        return user;
    }

}

