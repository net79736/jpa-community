package com.jpacommunity.member.repository;

import com.jpacommunity.member.entity.Oauth2Member;

import java.util.Optional;

public interface Oauth2MemberRepository {
    Optional<Oauth2Member> findByEmail(String email);
}
