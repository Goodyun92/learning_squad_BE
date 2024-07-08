package com.capstone.learning_squad_be.service;

import com.capstone.learning_squad_be.domain.Answer;
import com.capstone.learning_squad_be.domain.Document;
import com.capstone.learning_squad_be.domain.Question;
import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import com.capstone.learning_squad_be.dto.document.DocumentUploadRequestDto;
import com.capstone.learning_squad_be.dto.document.DocumentUploadReturnDto;
import com.capstone.learning_squad_be.dto.mydocs.DocumentInfo;
import com.capstone.learning_squad_be.dto.mydocs.MydocsReturnDto;
import com.capstone.learning_squad_be.dto.mydocs.QuestionInfo;
import com.capstone.learning_squad_be.exception.AppException;
import com.capstone.learning_squad_be.repository.AnswerRepository;
import com.capstone.learning_squad_be.repository.DocumentRepository;
import com.capstone.learning_squad_be.domain.user.User;
import com.capstone.learning_squad_be.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    private final QuestionService questionService;

    public boolean isPdf(String url){
        String fileExtension = getFileExtension(url);

        // PDF 파일인지 판단, equalsIgnoreCase 대소문자 무시
        if (!fileExtension.equalsIgnoreCase("pdf")) {
            return false;
        }

        return true;
    }

    //리턴 dto 타입
    @Transactional
    public Mono<DocumentUploadReturnDto> upload(DocumentUploadRequestDto dto, User user){
        try {
            // S3 URL로부터 PDF 파일 다운로드
            URL url = new URL(dto.getDocumentUrl());
            URLConnection connection = url.openConnection();
            PDDocument document = PDDocument.load(connection.getInputStream());
            if (!validateDocument(document)){
                //pdf 반려 예외처리
                new AppException(ErrorCode.SHORT_PDF_LENGTH, "PDF 길이가 너무 짧습니다.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            new AppException(ErrorCode.PDF_PROCESSING_ERR, "PDF 처리 과정에서 오류가 발생했습니다.");
        }

        Document document = Document.builder()
                .url(dto.getDocumentUrl())
                .title(dto.getTitle())
                .user(user)
                .build();
        documentRepository.save(document);

        log.info("documentUrl:{}",document.getUrl());

        // 문제 만들기 호출 문제수 리턴받기
        return questionService.createQuestion(dto.getDocumentUrl(), document)
                .flatMap(questionSize -> {
                    if (questionSize < 0) {
                        return Mono.error(new AppException(ErrorCode.MODEL_SERVER_ERR, "모델 서버 응답 에러, 잠시 후 다시 시도해주세요."));
                    }
                    log.info("questionSize:{}", questionSize);

                    // 문제수 document 업데이트
                    document.setQuestioinSize(questionSize);
                    documentRepository.save(document);

                    DocumentUploadReturnDto returnDto = DocumentUploadReturnDto.builder()
                            .id(document.getId())
                            .title(document.getTitle())
                            .questionSize(document.getQuestioinSize())
                            .build();

                    return Mono.just(returnDto);
                });
    }

    private boolean validateDocument(PDDocument document){

        try{
            // PDF 문서에서 텍스트 추출
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);

            // PDF 파일에서 공백을 포함하여 글자 수 계산
            int charCount = text.length();

            // PDF 파일의 글자 수가 2000자 이하인지 확인하여 응답 반환
            if (charCount <= 2000) return false;
        }catch (IOException e){
            e.printStackTrace();
        }

        return true;

    }

    // 파일 확장자를 가져오는 메서드
    private String getFileExtension(String filePath) {
        int lastIndex = filePath.lastIndexOf('.');
        if (lastIndex == -1) {
            return ""; // 파일 확장자가 없는 경우
        }
        return filePath.substring(lastIndex + 1);
    }

    @Transactional
    public void delete(Long id, User user){
        Document selectedDocument = documentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND, "문서"+ id + "이 없습니다."));

        //본인의 문서가 아닌 경우
        if (selectedDocument.getUser().getId() != user.getId()){
            throw new AppException(ErrorCode.FORBIDDEN, "해당 문서에 대한 삭제 권한이 없습니다.");
        }

        //cascade 사용?
        //답변,문제,문서 순으로 삭제

        List<Question> questionList = questionRepository.findByDocument(selectedDocument);
        for (Question question : questionList){
            answerRepository.deleteByQuestion(question);
        }

        questionRepository.deleteAllByDocument(selectedDocument);

        documentRepository.delete(selectedDocument);
    }

    public MydocsReturnDto getMydocs(User user){
        List<Document> documents = documentRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND, "사용자"+ user.getUserName() +"의 문서가 없습니다."));

        List<DocumentInfo> documentInfos = new ArrayList<>();

        for (Document document : documents){
            List<Question> questions = questionRepository.findByDocument(document);

            List<QuestionInfo> questionInfos = new ArrayList<>();

            for (Question question : questions){
                Answer answer = answerRepository.findByQuestion(question);
                QuestionInfo questionInfo = QuestionInfo.builder()
                        .questionId(question.getId())
                        .questionNumber(question.getQuestionNumber())
                        .content(question.getContent())
                        .correctAnswer(answer.getCorrectAnswer())
                        .bestAnswer(answer.getBestAnswer())
                        .score(answer.getScore())
                        .build();
                questionInfos.add(questionInfo);
            }

            DocumentInfo documentInfo = DocumentInfo.builder()
                    .documentId(document.getId())
                    .title(document.getTitle())
                    .questions(questionInfos)
                    .build();

            documentInfos.add(documentInfo);
        }

        MydocsReturnDto returnDto = MydocsReturnDto.builder()
                .documents(documentInfos)
                .build();

        return returnDto;
    }

}
