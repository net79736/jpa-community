package com.jpacommunity.board.core.entity;


import com.jpacommunity.board.api.dto.PostCreateRequest;
import com.jpacommunity.board.api.dto.PostUpdateRequest;
import com.jpacommunity.common.domain.Base;
import com.jpacommunity.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "p_posts")
public class Post extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // 게시물의 카테고리

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public Post(PostCreateRequest postCreateRequest, Category category, Member member) {
        this.title = postCreateRequest.getTitle();
        this.content = postCreateRequest.getContent();
        this.category = category;
        this.member = member;
    }

    public Post(PostUpdateRequest postUpdateRequest, Category category) {
        this.title = postUpdateRequest.getTitle();
        this.content = postUpdateRequest.getContent();
        this.category = category;
    }

    public void update(PostUpdateRequest postUpdateRequest) {
        this.title = postUpdateRequest.getTitle();
        this.content = postUpdateRequest.getContent();
    }
}
