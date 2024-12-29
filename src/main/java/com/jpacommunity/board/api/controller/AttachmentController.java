package com.jpacommunity.board.api.controller;

import com.jpacommunity.board.api.controller.response.AttachmentResponse;
import com.jpacommunity.board.api.dto.PostCreateRequest;
import com.jpacommunity.board.core.service.AttachmentFileService;
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

    private final AttachmentFileService attachmentFileService;

    // RequestPart 참고
    // https://devsungwon.tistory.com/entry/Spring-MultipartFile%EC%9D%B4-%ED%8F%AC%ED%95%A8%EB%90%9C-DTO-requestBody%EB%A1%9C-%EC%9A%94%EC%B2%AD%EB%B0%9B%EA%B8%B0-swagger-%EC%9A%94%EC%B2%AD
    // CREATE: 첨부파일 생성
    @PostMapping("/v1")
    public ResponseEntity<ResponseDto<AttachmentResponse>> create(
            @RequestPart(value = "file", required = false) List<MultipartFile> files
    ) throws JpaCommunityException {
        AttachmentResponse response = null;
        log.info("AttachmentController create START");

        try {
            if (files.isEmpty()) {
                return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "첨부 파일 목록이 존재하지 않습니다", null));
            }
            response = attachmentFileService.create(files);
        } catch (IOException e) {
            log.error("파일 업로드 도중 I/O 에러가 발생하였습니다. errorMessage: {}", e.getMessage());
            throw new JpaCommunityException(IO_EXCEPTION);
        } catch (Exception e) {
            log.error("파일 업로드 도중 서버 에러가 발생하였습니다. errorMessage: {}", e.getMessage());
            throw new JpaCommunityException(INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "첨부파일 생성 성공", response));
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ResponseDto<AttachmentResponse>> createV2(
            @RequestPart("postCreateRequest") @Valid PostCreateRequest postCreateRequest,
            @RequestPart(value = "file", required = false) List<MultipartFile> files,
            HttpServletRequest request
    ) {
        log.info("AttachmentController createV2 START");
        AttachmentResponse response = null;
        print(postCreateRequest, files, request);

        try {
            if (files.isEmpty()) {
                return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "첨부 파일 목록이 존재하지 않습니다", null));
            }
            response = attachmentFileService.create(files);
        } catch (IOException e) {
            log.error("파일 업로드 도중 I/O 에러가 발생하였습니다. errorMessage: {}", e.getMessage());
            throw new JpaCommunityException(IO_EXCEPTION);
        } catch (Exception e) {
            log.error("파일 업로드 도중 서버 에러가 발생하였습니다. errorMessage: {}", e.getMessage());
            throw new JpaCommunityException(INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "첨부파일 생성 성공", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> delete(@PathVariable Long id) {
        attachmentFileService.delete(id);
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
