package com.jpacommunity.board.core.service;

import com.jpacommunity.board.api.controller.response.PostResponse;
import com.jpacommunity.board.api.dto.PostCreateRequest;
import com.jpacommunity.board.api.dto.PostUpdateRequest;
import com.jpacommunity.board.api.dto.validator.PostCreateRequestValidator;
import com.jpacommunity.board.core.entity.Category;
import com.jpacommunity.board.core.entity.Post;
import com.jpacommunity.board.core.repository.CategoryRepository;
import com.jpacommunity.board.core.repository.PostJpaRepository;
import com.jpacommunity.global.exception.JpaCommunityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.HashMap;
import java.util.Map;

import static com.jpacommunity.global.exception.ErrorCode.INVALID_PARAMETER;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostJpaRepository postJpaRepository;
    private final CategoryRepository categoryRepository;
    private final PostCreateRequestValidator postCreateRequestValidator;

    @Transactional
    public PostResponse create(PostCreateRequest postCreateRequest) {
        // Category 엔티티 조회
        Category category = categoryRepository.getById(postCreateRequest.getCategoryId());

        // Post 생성 및 저장
        Post post = new Post(postCreateRequest, category);
        System.out.println("여기까지 문제가 없음...1");
        Post save = postJpaRepository.save(post);
        System.out.println("여기까지 문제가 없음...2");
        return new PostResponse(save);
    }

    @Transactional
    public PostResponse update(PostUpdateRequest postUpdateRequest) {
        // Category 엔티티 조회
        Long postId = postUpdateRequest.getId();
        Post post = postJpaRepository.getOne(postId);

        post.update(postUpdateRequest);

        // Post 생성 및 저장
        return new PostResponse(post);
    }

    @Transactional
    public PostResponse delete(long id) {
        Post post = postJpaRepository.getOne(id);

        postJpaRepository.delete(post);

        return new PostResponse(post);
    }

    public void validateRequest(PostCreateRequest request) {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "postCreateRequest");
        postCreateRequestValidator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errorMap.put(error.getField(), error.getDefaultMessage()));
            throw new JpaCommunityException(INVALID_PARAMETER, errorMap);
        }
    }
}
