package com.jpacommunity.board.core.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl {
    PostJpaRepository postJpaRepository;

}