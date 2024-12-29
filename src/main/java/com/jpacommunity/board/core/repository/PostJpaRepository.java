package com.jpacommunity.board.core.repository;

import com.jpacommunity.board.core.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends JpaRepository<Post, Long> {
}
