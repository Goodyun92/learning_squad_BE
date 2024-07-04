package com.capstone.learning_squad_be.service;

import com.capstone.learning_squad_be.domain.Answer;
import com.capstone.learning_squad_be.domain.Document;
import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import com.capstone.learning_squad_be.domain.user.User;
import com.capstone.learning_squad_be.dto.answer.AnswerPostReturnDto;
import com.capstone.learning_squad_be.dto.flask.GetSimilarityRequestDto;
import com.capstone.learning_squad_be.dto.flask.GetSimilarityReturnDto;
import com.capstone.learning_squad_be.exception.AppException;
import com.capstone.learning_squad_be.repository.AnswerRepository;
import com.capstone.learning_squad_be.repository.QuestionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerService {

    @Value("${flask.similarity.base-url}")
    private String baseUrl;

    private final AnswerRepository answerRepository;
    private final WebClient webClient;

    private static final String NAME = "lsls";
    private static final String FALLBACK = "getSimilarityFallback";

    public Mono<AnswerPostReturnDto> postAnswer(Answer answer, String userAnswer, User user){
        User ownerUser = answer.getQuestion().getDocument().getUser();

        if(!ownerUser.equals(user)){
            new AppException(ErrorCode.FORBIDDEN, "문제 답변 입력에 대한 권한이 없습니다.");
        }

        String correctAnswer = answer.getCorrectAnswer();

        return getSimilarity(correctAnswer, userAnswer)
                .flatMap(newScore -> {
                    if (newScore < 0) {
                        return Mono.error(new AppException(ErrorCode.MODEL_SERVER_ERR, "모델 서버 응답 에러, 잠시 후 다시 시도해주세요."));
                    }

                    if (newScore > answer.getScore()) {
                        answer.setBestAnswer(userAnswer);
                        answer.setScore(newScore);
                        answerRepository.save(answer);
                    }

                    AnswerPostReturnDto returnDto = AnswerPostReturnDto.builder()
                            .id(answer.getId())
                            .newScore(newScore)
                            .bestScore(answer.getScore())
                            .userAnswer(userAnswer)
                            .bestAnswer(answer.getBestAnswer())
                            .correctAnswer(answer.getCorrectAnswer())
                            .build();

                    return Mono.just(returnDto);
                });
    }

    @CircuitBreaker(name = NAME, fallbackMethod = FALLBACK)
    private Mono<Integer> getSimilarity(String correctAnswer, String userAnswer){
        GetSimilarityRequestDto requestDto = GetSimilarityRequestDto.builder()
                .sentence1(correctAnswer)
                .sentence2(userAnswer)
                .build();

        return webClient.post()
                .uri(baseUrl+"/get-similarity")
                .body(BodyInserters.fromValue(requestDto))
                .retrieve()
                .bodyToMono(GetSimilarityReturnDto.class)
                .map(GetSimilarityReturnDto::getSimilarity_score);
    }

    private Mono<Integer> getSimilarityFallback(String correctAnswer, String userAnswer, Throwable t){
        log.error("Fallback : "+ t.getMessage());
        return Mono.just(-1); // fallback data
    }

}
