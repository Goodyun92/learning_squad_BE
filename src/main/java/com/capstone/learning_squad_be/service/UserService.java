package com.capstone.learning_squad_be.service;

import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import com.capstone.learning_squad_be.domain.user.User;
import com.capstone.learning_squad_be.dto.user.UserJoinRequestDto;
import com.capstone.learning_squad_be.exception.AppException;
import com.capstone.learning_squad_be.repository.user.UserRepository;
import com.capstone.learning_squad_be.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static com.capstone.learning_squad_be.domain.enums.Role.ROLE_USER;

@Service
//@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Value("${jwt.token.secret}")
    private String acessKey; //secret key

    @Value("${jwt.token.refresh}")
    private String refreshKey;

    //런타임에 의존성 주입 (생성자 주입 방식)
    //생성자 직접 작성
    public UserService(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

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
        return getAccessToken(selectedUser.getUserName());
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

    public String getAccessToken(String userName){
//        Long accessExpireTimeMs = 1000 * 60 * 60L;
        Long accessExpireTimeMs = 1000 *60 * 60 *24 *30L;  //한달로 설정 임시
//        Long accessExpireTimeMs = 1000 * 60 * 1L;   //test

        User selectedUser = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.USERNAME_NOT_FOUND, "사용자"+userName + "이 없습니다."));

        return JwtTokenUtil.createToken(selectedUser.getUserName(), selectedUser.getRole(),acessKey, accessExpireTimeMs);
    }

    public String getRefreshToken(String userName){
        Long refreshExpireTimeMs = 1000 *60 * 60 *24 *30L;
//        Long refreshExpireTimeMs = 1000 *60 * 2L;   //test

        User selectedUser = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.USERNAME_NOT_FOUND, userName + "이 없습니다."));

        return JwtTokenUtil.createToken(selectedUser.getUserName(), selectedUser.getRole() ,refreshKey, refreshExpireTimeMs);
    }

    public boolean validateRefreshToken(String refreshToken){
        return JwtTokenUtil.isValidate(refreshToken,refreshKey);
    }

    public String getUserNameByRefreshToken(String refreshToken){
        return JwtTokenUtil.getUserName(refreshToken,refreshKey);
    }

}

