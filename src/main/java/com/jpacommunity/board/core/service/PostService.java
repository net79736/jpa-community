package com.jpacommunity.board.core.service;

import com.jpacommunity.board.api.controller.response.PostResponse;
import com.jpacommunity.board.api.dto.PostCreateRequest;
import com.jpacommunity.board.api.dto.PostUpdateRequest;
import com.jpacommunity.board.core.entity.Category;
import com.jpacommunity.board.core.entity.Post;
import com.jpacommunity.board.core.repository.CategoryRepository;
import com.jpacommunity.board.core.repository.PostJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostJpaRepository postJpaRepository;
    private final CategoryRepository categoryRepository;

    public PostResponse create(PostCreateRequest postCreateRequest) {
        // Category 엔티티 조회
        Category category = categoryRepository.getById(postCreateRequest.getCategoryId());

        // Post 생성 및 저장
        Post post = new Post(postCreateRequest, category);
        return new PostResponse(postJpaRepository.save(post));
    }

    public PostResponse update(PostUpdateRequest postUpdateRequest) {
        // Category 엔티티 조회
        Long postId = postUpdateRequest.getId();
        Post post = postJpaRepository.getOne(postId);

        post.update(postUpdateRequest);

        // Post 생성 및 저장
        return new PostResponse(post);
    }

    public PostResponse delete(long id) {
        Post post = postJpaRepository.getOne(id);

        postJpaRepository.delete(post);

        return new PostResponse(post);
    }
}
