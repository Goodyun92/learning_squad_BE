package com.capstone.learning_squad_be.service;

import com.capstone.learning_squad_be.domain.Answer;
import com.capstone.learning_squad_be.dto.answer.AnswerPostReturnDto;
import com.capstone.learning_squad_be.dto.flask.GetSimilarityRequestDto;
import com.capstone.learning_squad_be.dto.flask.GetSimilarityReturnDto;
import com.capstone.learning_squad_be.repository.AnswerRepository;
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

    @Value("${flask.base-url}")
    private String baseUrl;

    private final AnswerRepository answerRepository;
    private final RestTemplate restTemplate;

    public AnswerPostReturnDto postAnswer(Answer answer, String userAnswer){
        String correctAnswer = answer.getCorrectAnswer();

        Integer newScore = getSimilarity(correctAnswer,userAnswer);

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

    private Integer getSimilarity(String correctAnswer, String userAnswer){
        GetSimilarityRequestDto requestDto = GetSimilarityRequestDto.builder()
                .sentence1(correctAnswer)
                .sentence2(userAnswer)
                .build();

        String url = "/get-similarity";

        ResponseEntity<GetSimilarityReturnDto> response = restTemplate.postForEntity(baseUrl+url,requestDto,GetSimilarityReturnDto.class);

        return response.getBody().getSimilarity_score();

    }
}
