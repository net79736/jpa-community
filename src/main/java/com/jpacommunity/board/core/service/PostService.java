package com.jpacommunity.board.core.service;

import com.jpacommunity.board.api.controller.response.PostResponse;
import com.jpacommunity.board.api.dto.PostCreateRequest;
import com.jpacommunity.board.api.dto.PostUpdateRequest;
import com.jpacommunity.board.core.entity.Category;
import com.jpacommunity.board.core.entity.Post;
import com.jpacommunity.board.core.repository.post.PostRepository;
import com.jpacommunity.global.exception.ErrorCode;
import com.jpacommunity.global.exception.JpaCommunityException;
import com.jpacommunity.member.entity.Member;
import com.jpacommunity.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.jpacommunity.global.exception.ErrorCode.RESOURCE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final CategoryService categoryService;
    private final AttachmentService attachmentService;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PostResponse create(PostCreateRequest postCreateRequest, UUID publicId) throws Exception {
        // 1. 게시물 데이터 작성
        Category category = categoryService.fetchById(postCreateRequest.getCategoryId());

        // 2. 멤버 객체 조회 (작성자)
        Member member = memberRepository.getByPublicId(publicId);

        // 3. Post 생성 및 저장
        Post post = new Post(postCreateRequest, category, member);

        postRepository.save(post);

        // Post 생성 및 저장
        return new PostResponse(post.getId());
    }

    /**
     * 게시글을 수정한다.
     *
     * @param postUpdateRequest 첨부파일 내용을 제외한 게시글 수정 객체
     * @return
     * @throws Exception
     */
    @Transactional
    public PostResponse update(PostUpdateRequest postUpdateRequest) throws Exception {
        // 1. 게시글 수정
        Post post = fetchById(postUpdateRequest.getId());
        post.update(postUpdateRequest);

        // Post 생성 및 저장
        return new PostResponse(post);
    }

    // 단일 게시글 삭제
    @Transactional
    public PostResponse delete(long id) {
        // 삭제 게시글 조회
        Post post = fetchById(id);

        // 외래키 관계로 먼저 삭제해야 함
        // 첨부파일 관련 DB 삭제
        attachmentService.deleteByPostId(id);

        // DB 삭제
        postRepository.delete(post);

        // 삭제된 게시글 Id 를 담은 Response 반환
        return new PostResponse(post.getId());
    }

    // 여러 게시글 삭제
    @Transactional
    public PostResponse deleteAllByIdIn(List<Long> ids) {
        List<Long> deletedIds = new ArrayList();
        if (ids == null || ids.isEmpty()) {
            throw new JpaCommunityException(ErrorCode.INVALID_PARAMETER, "삭제할 ID 목록이 비어 있습니다.");
        }

        for (Long id : ids) {
            deletedIds.add(delete(id).getId());
        }

        return new PostResponse(deletedIds);
    }

    // 공통 메서드: Category 엔티티를 반환
    public Post fetchById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new JpaCommunityException(RESOURCE_NOT_FOUND, "id -> " + id + " 는 존재하지 않는 게시글 입니다"));
    }

    /**
     * Post 객체를 ID로 조회
     * @param postId 게시물 ID
     * @return 조회된 Post 객체
     */
    private PostResponse getById(Long postId) {
        return new PostResponse(fetchById(postId));
    }
}
