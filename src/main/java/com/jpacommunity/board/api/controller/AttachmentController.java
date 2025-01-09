package com.jpacommunity.board.api.controller;

import com.jpacommunity.board.api.controller.response.AttachmentResponse;
import com.jpacommunity.board.api.dto.PostCreateRequest;
import com.jpacommunity.board.core.service.AttachmentService;
import com.jpacommunity.common.web.response.ResponseDto;
import com.jpacommunity.global.exception.JpaCommunityException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.jpacommunity.common.web.response.ResponseStatus.SUCCESS;
import static com.jpacommunity.global.exception.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.jpacommunity.global.exception.ErrorCode.IO_EXCEPTION;


@Slf4j
@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {
    private static final String TEMP_DIRECTORY = "temp-files"; // 경로 설정

    private final AttachmentService attachmentService;

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> delete(@PathVariable Long id) {
        attachmentService.deleteById(id);
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "첨부파일 삭제 성공", null));
    }

    private static void print(PostCreateRequest postCreateRequest, List<MultipartFile> files, HttpServletRequest request) {
        String realPath = request.getServletContext().getRealPath("/upload/");
        String realPath2 = request.getServletContext().getContextPath();
        String realPath3 = request.getServletContext().getRealPath("/static");
        System.out.println("========= 출력 ========");
        System.out.println("realPath: " + realPath);
        System.out.println("realPath2: " + realPath2);
        System.out.println("realPath3: " + realPath3);
        System.out.println(postCreateRequest.getContent());
        System.out.println(postCreateRequest.getTitle());
        System.out.println(postCreateRequest.getCategoryId());
        System.out.println(files);
        System.out.println(files.size());
    }
}
