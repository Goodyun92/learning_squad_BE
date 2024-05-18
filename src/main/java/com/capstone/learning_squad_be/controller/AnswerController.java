package com.capstone.learning_squad_be.controller;

import com.capstone.learning_squad_be.domain.Answer;
import com.capstone.learning_squad_be.domain.Question;
import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import com.capstone.learning_squad_be.dto.answer.AnswerPostRequestDto;
import com.capstone.learning_squad_be.dto.answer.AnswerPostReturnDto;
import com.capstone.learning_squad_be.dto.common.ReturnDto;
import com.capstone.learning_squad_be.exception.AppException;
import com.capstone.learning_squad_be.repository.AnswerRepository;
import com.capstone.learning_squad_be.repository.QuestionRepository;
import com.capstone.learning_squad_be.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/answers")
public class AnswerController {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final AnswerService answerService;

    @PostMapping
    public ReturnDto<AnswerPostReturnDto> postAnswer (@RequestBody AnswerPostRequestDto dto){
        Long questionId = dto.getQuestionId();

        Question question = questionRepository.findById(questionId)
            .orElseThrow(()->new AppException(ErrorCode.QUESTION_NOT_FOUND, "문제 Id : " + questionId + "이 없습니다."));

        Answer answer = answerRepository.findByQuestion(question);

        AnswerPostReturnDto returnDto = answerService.postAnswer(answer, dto.getUserAnswer());

        return ReturnDto.ok(returnDto);
    }
}
