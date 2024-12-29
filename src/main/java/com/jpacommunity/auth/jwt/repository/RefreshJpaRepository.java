package com.jpacommunity.jwt.repository;

import com.jpacommunity.jwt.entity.Refresh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface RefreshJpaRepository extends JpaRepository<Refresh, Long> {
    Boolean existsByRefreshAndPublicId(String refresh, UUID publicId);
    Optional<Refresh> findByPublicId(UUID publicId); // 테스트 용으로 추가함
    @Transactional
    void deleteByRefreshAndPublicId(String refresh, UUID publicId);
}
