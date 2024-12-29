package com.jpacommunity.board.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentRequest {
    @NotNull
    private Long postId;          // 게시물 ID
    @NotBlank
    private String filename;      // 파일 이름
    private Long size;        // 파일 크기
    @NotNull
    private List<MultipartFile> uploadedFiles; // 업로드된 다건의 파일
}