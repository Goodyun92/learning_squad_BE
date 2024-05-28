package com.capstone.learning_squad_be.service;

import com.capstone.learning_squad_be.domain.Answer;
import com.capstone.learning_squad_be.domain.Document;
import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import com.capstone.learning_squad_be.dto.answer.AnswerPostReturnDto;
import com.capstone.learning_squad_be.dto.flask.GetSimilarityRequestDto;
import com.capstone.learning_squad_be.dto.flask.GetSimilarityReturnDto;
import com.capstone.learning_squad_be.exception.AppException;
import com.capstone.learning_squad_be.repository.AnswerRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerService {

    @Value("${flask.similarity.base-url}")
    private String baseUrl;

    private final AnswerRepository answerRepository;
    private final RestTemplate restTemplate;

    private static final String NAME = "lsls";
    private static final String FALLBACK = "getSimilarityFallback";

    public AnswerPostReturnDto postAnswer(Answer answer, String userAnswer){
        String correctAnswer = answer.getCorrectAnswer();

        Integer newScore = getSimilarity(correctAnswer,userAnswer);

        if (newScore <0){
            new AppException(ErrorCode.MODEL_SERVER_ERR, "모델 서버 응답 에러, 잠시 후 다시 시도해주세요.");
        }

        if(newScore > answer.getScore()){
            answer.setBestAnswer(userAnswer);
            answer.setScore(newScore);
            answerRepository.save(answer);
        }

        AnswerPostReturnDto returnDto = AnswerPostReturnDto.builder()
                .newScore(newScore)
                .bestScore(answer.getScore())
                .userAnswer(userAnswer)
                .bestAnswer(answer.getBestAnswer())
                .correctAnswer(answer.getCorrectAnswer())
                .build();

        return returnDto;
    }

    @CircuitBreaker(name = NAME, fallbackMethod = FALLBACK)
    private Integer getSimilarity(String correctAnswer, String userAnswer){
        GetSimilarityRequestDto requestDto = GetSimilarityRequestDto.builder()
                .sentence1(correctAnswer)
                .sentence2(userAnswer)
                .build();

        String url = "/get-similarity";

        ResponseEntity<GetSimilarityReturnDto> response = restTemplate.postForEntity(baseUrl+url,requestDto,GetSimilarityReturnDto.class);

        return response.getBody().getSimilarity_score();
    }

    private Integer getSimilarityFallback(String correctAnswer, String userAnswer, Throwable t){
        log.error("Fallback : "+ t.getMessage());
        return -1; // fallback data
    }
}
