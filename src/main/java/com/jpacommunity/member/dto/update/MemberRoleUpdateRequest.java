package com.jpacommunity.member.dto.update;

import com.jpacommunity.member.domain.MemberRole;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberRoleUpdateRequest {
    @Pattern(regexp = "^[0-9a-zA-Z]+@[0-9a-zA-Z]+(\\.[a-zA-Z]{2,3}){1,2}$", message = "이메일 형식으로 작성해주세요")
    private String email; // 계정
    private MemberRole role;
    private String clientId;
    private String secretKey;
}
