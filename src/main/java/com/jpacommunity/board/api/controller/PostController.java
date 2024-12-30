package com.jpacommunity.board.api.controller;

import com.jpacommunity.board.api.controller.response.PostResponse;
import com.jpacommunity.board.api.dto.AttachmentRequest;
import com.jpacommunity.board.api.dto.PostCreateRequest;
import com.jpacommunity.board.api.dto.PostUpdateRequest;
import com.jpacommunity.board.core.service.AttachmentFileService;
import com.jpacommunity.board.core.service.PostService;
import com.jpacommunity.common.web.response.ResponseDto;
import com.jpacommunity.global.exception.JpaCommunityException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.jpacommunity.common.web.response.ResponseStatus.SUCCESS;
import static com.jpacommunity.global.exception.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.jpacommunity.global.exception.ErrorCode.IO_EXCEPTION;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final AttachmentFileService attachmentFileService;

    // CREATE: 카테고리 생성
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseDto<PostResponse>> create(
            @Valid @RequestPart("postCreateRequest") PostCreateRequest postCreateRequest,
            BindingResult bindingResult,
            @RequestPart(value = "file", required = false) List<MultipartFile> files
    ) {
        log.info("PostController create START");
        log.info("PostController create Content : {}", postCreateRequest.getContent());
        log.info("PostController create Title : {}", postCreateRequest.getTitle());
        log.info("PostController create CategoryId : {}", postCreateRequest.getCategoryId());
        PostResponse postResponse = null;

        try {
            // 게시물 데이터 작성
            postResponse = postService.create(postCreateRequest);

            // 첨부파일 업로드
            if (files != null && !files.isEmpty()) {
                System.out.println("여기 들어온다고요 ????");
                List<AttachmentRequest> attachmentRequests = attachmentFileService.generateAttachmentRequests(postResponse.getId(), files);
                attachmentFileService.create(files, attachmentRequests);
            }
        } catch (IOException e) {
            log.error("파일 업로드 도중 I/O 에러가 발생하였습니다. errorMessage: {}", e.getMessage());
            throw new JpaCommunityException(IO_EXCEPTION);
        } catch (Exception e) {
            log.error("파일 업로드 도중 서버 에러가 발생하였습니다. errorMessage: {}", e.getMessage());
            throw new JpaCommunityException(INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "게시글 작성 성공", postResponse));
    }

    // UPDATE: 카테고리 수정
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<PostResponse>> update(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest postUpdateRequest) {
        postService.update(postUpdateRequest);
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "게시글 수정 성공", null));
    }

    // DELETE: 카테고리 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "게시글 삭제 성공", null));
    }

    // READ: 단일 카테고리 조회
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<PostResponse>> get(@PathVariable Long id) {
        // PostResponse response = postService.getById(id);
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "게시글 조회 성공", null));
    }

    @GetMapping()
    public ResponseEntity<ResponseDto<List<PostResponse>>> list() {
        // final List<PostResponse> categories = postService.getAllCategories();
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "게시글 목록 조회 성공", null));
    }

    @GetMapping("/roots")
    public ResponseEntity<ResponseDto<List<PostResponse>>> getRootCategories() {
        // List<PostResponse> rootCategories = postService.getRootCategories();
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "게시글 조회 성공", null));
    }
}
