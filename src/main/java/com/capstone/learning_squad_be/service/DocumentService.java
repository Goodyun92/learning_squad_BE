package com.capstone.learning_squad_be.service;

import com.capstone.learning_squad_be.domain.Document;
import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import com.capstone.learning_squad_be.dto.document.DocumentUploadRequestDto;
import com.capstone.learning_squad_be.dto.document.DocumentUploadReturnDto;
import com.capstone.learning_squad_be.exception.AppException;
import com.capstone.learning_squad_be.repository.DocumentRepository;
import com.capstone.learning_squad_be.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
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
    public DocumentUploadReturnDto upload(DocumentUploadRequestDto dto, User user){
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

        //문제 만들기 호출 문제수 리턴받기
        Integer questionSize = questionService.createQuestion(dto.getDocumentUrl(),document);
        log.info("questionSize:{}",questionSize);

        //문제수 document 업데이트
        document.setQuestioinSize(questionSize);
        documentRepository.save(document);

        DocumentUploadReturnDto returnDto =
                DocumentUploadReturnDto.builder()
                        .id(document.getId())
                        .title(document.getTitle())
                        .questionSize(document.getQuestioinSize())
                        .build();

        return  returnDto;
        //리턴 dto로 리턴
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

}
