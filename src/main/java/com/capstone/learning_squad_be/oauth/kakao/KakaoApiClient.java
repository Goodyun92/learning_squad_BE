package com.capstone.learning_squad_be.oauth.kakao;

import com.capstone.learning_squad_be.oauth.OAuthApiClient;
import com.capstone.learning_squad_be.oauth.OAuthInfoResponse;
import com.capstone.learning_squad_be.oauth.OAuthLoginParams;
import com.capstone.learning_squad_be.oauth.OAuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoApiClient implements OAuthApiClient {

    /*
     * 외부 요청 후 미리 정의해둔 KakaoTokens, KakaoInfoResponse 로 응답값을 받는다.
     */

    private static final String GRANT_TYPE = "authorization_code";

    @Value("${security.oauth2.client.provider.kakao.token-uri}")
    private String authUrl;

    @Value("${security.oauth2.client.provider.kakao.user-info-uri}")
    private String apiUrl;

    @Value("${security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;


    @Override
    public OAuthProvider oAuthProvider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public String requestAccessToken(OAuthLoginParams params) {
        String url = authUrl;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = params.makeBody();
        body.add("grant_type", GRANT_TYPE);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<?> request = new HttpEntity<>(body, httpHeaders);

        KakaoTokens response = restTemplate.postForObject(url, request, KakaoTokens.class);

        assert response != null;

        return response.getAccessToken();
    }

    @Override
    public OAuthInfoResponse requestOauthInfo(String accessToken) {
        log.info("kakao accesstoken:{}", accessToken);

        String url = apiUrl;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + accessToken);
        httpHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> userInfoRequest = new HttpEntity<>(httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<KakaoInfoResponse> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                userInfoRequest,
                KakaoInfoResponse.class
        );

        log.info("kakao response:{}", response.getBody());

//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("property_keys", "[\"kakao_account.email\", \"kakao_account.profile\"]");

//        HttpEntity<?> request = new HttpEntity<>(body, httpHeaders);

//        return restTemplate.postForObject(url, request, KakaoInfoResponse.class);

        return response.getBody();
    }
}
