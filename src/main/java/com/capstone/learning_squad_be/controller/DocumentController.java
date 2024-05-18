package com.capstone.learning_squad_be.controller;

import com.capstone.learning_squad_be.domain.Document;
import com.capstone.learning_squad_be.domain.enums.ErrorCode;
import com.capstone.learning_squad_be.domain.user.User;
import com.capstone.learning_squad_be.dto.common.ReturnDto;
import com.capstone.learning_squad_be.dto.document.DocumentUploadRequestDto;
import com.capstone.learning_squad_be.dto.document.DocumentUploadReturnDto;
import com.capstone.learning_squad_be.dto.mydocs.MydocsReturnDto;
import com.capstone.learning_squad_be.exception.AppException;
import com.capstone.learning_squad_be.repository.DocumentRepository;
import com.capstone.learning_squad_be.repository.user.UserRepository;
import com.capstone.learning_squad_be.security.CustomUserDetail;
import com.capstone.learning_squad_be.service.DocumentService;
import com.capstone.learning_squad_be.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public ReturnDto<DocumentUploadReturnDto> upload(@RequestBody DocumentUploadRequestDto dto, @AuthenticationPrincipal CustomUserDetail customUserDetail){

        log.info("upload api start!");

        log.info("url:{}",dto.getDocumentUrl());

        if(!documentService.isPdf(dto.getDocumentUrl())){
            //pdf 아님 예외처리
            new AppException(ErrorCode.NOT_PDF, "PDF 확장자가 아닙니다.");
            log.info("not pdf error");
        }

        //userName 추출
        String userName = customUserDetail.getUser().getUserName();
        User user = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.USERNAME_NOT_FOUND, "사용자" + userName + "이 없습니다."));
        log.info("userName:{}",userName);

        //return dto로 받기
        DocumentUploadReturnDto returnDto = documentService.upload(dto,user);
        log.info("returnDto:{}",returnDto);

        return ReturnDto.ok(returnDto);
    }

    @DeleteMapping("/delete")
    public ReturnDto<Void> delete(@RequestParam Long id, @AuthenticationPrincipal CustomUserDetail customUserDetail){
        //userName 추출
        String userName = customUserDetail.getUser().getUserName();

        User user = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.USERNAME_NOT_FOUND, "사용자" + userName + "이 없습니다."));

        documentService.delete(id,user);

        return ReturnDto.ok();
    }

    @GetMapping("/mydocs")
    public ReturnDto<MydocsReturnDto> getMydocs(@AuthenticationPrincipal CustomUserDetail customUserDetail){
        //userName 추출
        String userName = customUserDetail.getUser().getUserName();

        User user = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.USERNAME_NOT_FOUND, "사용자" + userName + "이 없습니다."));

        MydocsReturnDto returnDto = documentService.getMydocs(user);

        return ReturnDto.ok(returnDto);
    }


}
