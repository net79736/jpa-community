package com.jpacommunity.global.security.filter;

import com.jpacommunity.member.domain.MemberRole;
import com.jpacommunity.member.domain.MemberStatus;
import org.junit.jupiter.api.Test;

class TokenAuthenticationFilterTest {

    // END 는 자동 완성 후 커서 위치
    @Test
    public void roletest() throws Exception {
        String role = "ADMIN";

        MemberRole memberRole = MemberRole.valueOf(role);

        System.out.println(memberRole);
        System.out.println(memberRole.name());

        String status = "ACTIVE";

        MemberStatus memberStatus = MemberStatus.valueOf(status);

        System.out.println(memberStatus);
        System.out.println(memberStatus.name());
    }

}