package com.capstone.learning_squad_be.service;

import com.capstone.learning_squad_be.domain.Answer;
import com.capstone.learning_squad_be.domain.Document;
import com.capstone.learning_squad_be.domain.Question;
import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import com.capstone.learning_squad_be.dto.flask.CreateQuestionRequestDto;
import com.capstone.learning_squad_be.dto.flask.CreateQuestionReturnDto;
import com.capstone.learning_squad_be.exception.AppException;
import com.capstone.learning_squad_be.repository.AnswerRepository;
import com.capstone.learning_squad_be.repository.QuestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

//    private final FlaskApiClient flaskApiClient;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final RestTemplate restTemplate;

    public Integer createQuestion(String documentUrl, Document document){

        CreateQuestionRequestDto requestDto = CreateQuestionRequestDto.builder()
                .s3_url(documentUrl)
                .build();

        String url = "/test-s3-url";

        ResponseEntity<CreateQuestionReturnDto> response = restTemplate.postForEntity(url,requestDto,CreateQuestionReturnDto.class);

        String csvUrl = response.getBody().getCsv_url();
        log.info("csvUrl:{}",csvUrl);

        //문제 수 리턴
         return processQuestionFromCSV(csvUrl,document);

    }

    @Transactional
    public Integer processQuestionFromCSV(String csvUrl, Document document) {
        Integer questionCount = 0;

        try {
            URL url = new URL(csvUrl);
            URLConnection connection = url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            CSVReader csvReader = new CSVReader(reader);

            String[] nextLine;

            // 헤더 행을 건너뛰기
            csvReader.readNext();

            while ((nextLine = csvReader.readNext()) != null) {
                // 각 행에서 question과 answer를 읽음
                String readQuestion = nextLine[0];
                String readAnswer = nextLine[1];

                log.info("question:{}",readQuestion);
                log.info("answer:{}",readAnswer);

                Question question = Question.builder()
                        .content(readQuestion)
                        .document(document)
                        .questionNumber(++questionCount)    //문제 수 증가
                        .build();
                questionRepository.save(question);

                Answer answer = Answer.builder()
                        .correctAnswer(readAnswer)
                        .question(question)
                        .score(0)
                        .build();
                answerRepository.save(answer);
            }

            log.info("questionCount:{}",questionCount);

            csvReader.close();
            reader.close();

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            // 예외 처리
            new AppException(ErrorCode.CSV_PROCESSING_ERR, "CSV 처리 과정에서 오류가 발생했습니다.");
        }

        return questionCount;
    }
}
