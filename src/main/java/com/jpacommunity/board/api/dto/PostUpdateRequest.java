package com.jpacommunity.board.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateRequest {
    @NotNull
    private Long id; // 게시물의 Id
    @NotNull
    @Pattern(regexp = "^(?=.*[a-zA-Zㄱ-ㅎ가-힣0-9])[a-zㄱ-ㅎ가-힣A-Z0-9._\\-\\s]{4,20}$", message = "한글/영문/숫자 및 띄어쓰기, 4~20자 이내로 작성해주세요")
    private String title;
    @NotNull
    @Size(min = 4, max = 255)
    @Pattern(regexp = ".*\\S.*", message = "공백이나 줄바꿈만 입력할 수 없습니다.")
    private String content;
}
