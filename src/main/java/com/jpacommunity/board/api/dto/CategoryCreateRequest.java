package com.jpacommunity.board.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryCreateRequest {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z가-힣0-9_\\-\\s]{1,20}$", message = "한글/영문 1~20자 이내로 작성해주세요")
    private String name;
    private Long parentId; // 부모 카테고리의 ID

    @NotNull
    @Min(value = 0, message = "깊이는 0 이상이어야 합니다.")
    private Integer depth;

    private String link;

    // 받지 않기로 함 (자동생성)
    // @Min(value = 1, message = "순서는 1 이상이어야 합니다.")
    // private Integer orderIndex = 1;
}