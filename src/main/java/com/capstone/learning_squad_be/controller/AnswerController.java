package com.capstone.learning_squad_be.controller;

import com.capstone.learning_squad_be.domain.Answer;
import com.capstone.learning_squad_be.domain.Question;
import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import com.capstone.learning_squad_be.domain.user.User;
import com.capstone.learning_squad_be.dto.answer.AnswerPostRequestDto;
import com.capstone.learning_squad_be.dto.answer.AnswerPostReturnDto;
import com.capstone.learning_squad_be.dto.common.ReturnDto;
import com.capstone.learning_squad_be.exception.AppException;
import com.capstone.learning_squad_be.repository.AnswerRepository;
import com.capstone.learning_squad_be.repository.QuestionRepository;
import com.capstone.learning_squad_be.repository.user.UserRepository;
import com.capstone.learning_squad_be.security.CustomUserDetail;
import com.capstone.learning_squad_be.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final UserRepository userRepository;
    private final AnswerService answerService;

    @PostMapping
    public ReturnDto<AnswerPostReturnDto> postAnswer (@RequestBody AnswerPostRequestDto dto, @AuthenticationPrincipal CustomUserDetail customUserDetail){
        String userName = customUserDetail.getUser().getUserName();
        User user = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.USERNAME_NOT_FOUND, "사용자" + userName + "이 없습니다."));

        Long questionId = dto.getQuestionId();

        Question question = questionRepository.findById(questionId)
            .orElseThrow(()->new AppException(ErrorCode.QUESTION_NOT_FOUND, "문제 Id : " + questionId + "이 없습니다."));

        Answer answer = answerRepository.findByQuestion(question);

        AnswerPostReturnDto returnDto = answerService.postAnswer(answer, dto.getUserAnswer(), user);

        return ReturnDto.ok(returnDto);
    }
}
