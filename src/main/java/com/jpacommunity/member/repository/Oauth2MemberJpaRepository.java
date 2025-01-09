package com.jpacommunity.member.repository;

import com.jpacommunity.member.entity.Oauth2Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface Oauth2MemberJpaRepository extends JpaRepository<Oauth2Member, Long> {
    Optional<Oauth2Member> findByEmail(String email);
}

