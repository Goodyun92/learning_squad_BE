package com.capstone.learning_squad_be.oauth;

import com.capstone.learning_squad_be.domain.user.User;
import com.capstone.learning_squad_be.dto.oauth.TokensReturnDto;
import com.capstone.learning_squad_be.jwt.JwtService;
import com.capstone.learning_squad_be.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.capstone.learning_squad_be.domain.enums.Role.ROLE_USER;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RequestOAuthInfoService requestOAuthInfoService;

    public TokensReturnDto getTokens(OAuthLoginParams params) {
        OAuthInfoResponse oAuthInfoResponse = requestOAuthInfoService.request(params);

        User selectedUser = findOrCreateUser(oAuthInfoResponse);

        String accessToken= jwtService.getAccessToken(selectedUser.getUserName());

        String refreshToken = jwtService.getRefreshToken(selectedUser.getUserName());

        TokensReturnDto dto = TokensReturnDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return dto;
    }

    private User findOrCreateUser(OAuthInfoResponse oAuthInfoResponse) {
        return userRepository.findByUserName(oAuthInfoResponse.getEmail())
                .orElseGet(() -> newUser(oAuthInfoResponse));
    }

    private User newUser(OAuthInfoResponse oAuthInfoResponse) {

        String nickName = oAuthInfoResponse.getNickname() + "_" + oAuthInfoResponse.getOAuthProvider().toString();

        User user = User.builder()
                .userName(oAuthInfoResponse.getEmail())
                .nickName(nickName)
                .oAuthProvider(oAuthInfoResponse.getOAuthProvider())
                .role(ROLE_USER)
                .build();

        userRepository.save(user);

        return user;
    }
}
