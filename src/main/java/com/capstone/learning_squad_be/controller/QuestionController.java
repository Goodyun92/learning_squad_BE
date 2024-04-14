package com.capstone.learning_squad_be.controller;

import com.capstone.learning_squad_be.domain.Document;
import com.capstone.learning_squad_be.domain.Question;
import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import com.capstone.learning_squad_be.dto.common.ReturnDto;
import com.capstone.learning_squad_be.dto.question.QuestionGetReturnDto;
import com.capstone.learning_squad_be.exception.AppException;
import com.capstone.learning_squad_be.repository.DocumentRepository;
import com.capstone.learning_squad_be.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final DocumentRepository documentRepository;
    private final QuestionRepository questionRepository;

    @GetMapping
    public ReturnDto<QuestionGetReturnDto> getQuestion(@RequestParam Long documentId, @RequestParam Integer questionNum ){
        Document document = documentRepository.findById(documentId)
                .orElseThrow(()->new AppException(ErrorCode.DOCUMENT_NOT_FOUND, "문서 Id : " + documentId + "이 없습니다."));

        Question question = questionRepository.findFirstByQuestionNumberAndDocument(questionNum,document)
                .orElseThrow(()->new AppException(ErrorCode.QUESTION_NOT_FOUND, "해당 문서의 " + questionNum + "번째 문제가 존재하지 않습니다."));

        QuestionGetReturnDto returnDto = QuestionGetReturnDto.builder()
                .id(question.getId())
                .content(question.getContent())
                .build();

        return ReturnDto.ok(returnDto);
    }
}
