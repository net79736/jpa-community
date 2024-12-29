package com.jpacommunity.member.repository;

import com.jpacommunity.common.handler.exception.JpaCommunityException;
import com.jpacommunity.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static com.jpacommunity.common.handler.exception.ErrorCode.RESOURCE_NOT_FOUND;


@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;
    @Override
    public Optional<Member> findByNickname(String nickname) {
        return memberJpaRepository.findByNickname(nickname);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email);
    }

    @Override
    public Optional<Member> findByPublicId(UUID publicId) {
        return memberJpaRepository.findByPublicId(publicId);
    }

    @Override
    public Member getByNickname(String nickname) {
        return memberJpaRepository.findByNickname(nickname)
                .orElseThrow(() -> new JpaCommunityException(RESOURCE_NOT_FOUND, nickname + " 는 존재하지 않는 닉네임 입니다"));
    }

    @Override
    public Member getByEmail(String email) {
        return memberJpaRepository.findByEmail(email)
                .orElseThrow(() -> new JpaCommunityException(RESOURCE_NOT_FOUND, email + " 는 존재하지 않는 이메일 입니다"));
    }

    @Override
    public Member getByPublicId(UUID publicId) {
        return memberJpaRepository.findByPublicId(publicId)
                .orElseThrow(() -> new JpaCommunityException(RESOURCE_NOT_FOUND, publicId + " 는 존재하지 않는 PublicId 입니다"));
    }
}
