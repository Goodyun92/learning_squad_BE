package com.capstone.learning_squad_be.controller;

import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import com.capstone.learning_squad_be.domain.user.User;
import com.capstone.learning_squad_be.dto.common.ReturnDto;
import com.capstone.learning_squad_be.dto.user.UserJoinRequestDto;
import com.capstone.learning_squad_be.dto.user.UserLoginRequestDto;
import com.capstone.learning_squad_be.dto.user.UserTokenReturnDto;
import com.capstone.learning_squad_be.exception.AppException;
import com.capstone.learning_squad_be.repository.user.UserRepository;
import com.capstone.learning_squad_be.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/join")
    public ReturnDto<Void> join(@RequestBody UserJoinRequestDto dto){
        userService.join(dto);
        return ReturnDto.ok();
    }

    @PostMapping("/login")
    @ResponseBody
    public ReturnDto<UserTokenReturnDto> login(@RequestBody UserLoginRequestDto dto, HttpServletResponse response){
        String accessToken = userService.login(dto.getUserName(), dto.getPassword());

        // Refresh Token 생성
        String refreshToken = userService.getRefreshToken(dto.getUserName());

        Cookie cookie = new Cookie("refreshToken", refreshToken);

        // expires in 7 days
        cookie.setMaxAge(7 * 24 * 60 * 60);

        // optional properties
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        // add cookie to response
        response.addCookie(cookie);

        UserTokenReturnDto returnDto = UserTokenReturnDto.builder().token(accessToken).build();

        return ReturnDto.ok(returnDto);
    }

    @PutMapping("/updateNickName")
    public ReturnDto<User> updateNickName(@RequestParam String nickName, Authentication authentication) {
        // 현재 userName
        String userName = authentication.getName();

        User user = userService.updateNickName(userName, nickName);

        return ReturnDto.ok(user);
    }

    @GetMapping("/info")
    public ReturnDto<User> info(Authentication authentication){
        //userName 추출
        String userName = authentication.getName();

        User user = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.USERNAME_NOT_FOUND, "사용자" + userName + "이 없습니다."));

        return ReturnDto.ok(user);
    }

    @PostMapping("/refresh")
    public ReturnDto<UserTokenReturnDto> refresh (HttpServletRequest request){

        // 쿠키에서 Refresh Token 추출
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        //userName 추출
        String userName = userService.getUserNameByRefreshToken(refreshToken);
        String newAccessToken = null;

        log.info("refreshToken:{}",refreshToken);
        log.info("userName:{}",userName);

        if (userService.validateRefreshToken(refreshToken)) {
            // 새로운 Access Token 발급
            newAccessToken = userService.getAccessToken(userName);// 새로운 Access Token 생성 로직
        }

        UserTokenReturnDto returnDto = UserTokenReturnDto.builder().token(newAccessToken).build();

        return ReturnDto.ok(returnDto);

    }

}
