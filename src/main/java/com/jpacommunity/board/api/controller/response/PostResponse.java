package com.jpacommunity.board.api.controller.response;

import com.jpacommunity.board.core.entity.Post;
import lombok.Getter;

import java.util.List;

@Getter
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private Long categoryId;
    private String categoryName;
    private List<Long> ids;

    public PostResponse(Long id) {
        this.id = id;
    }

    public PostResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.categoryId = post.getCategory() != null ? post.getCategory().getId() : null;
        this.categoryName = post.getCategory() != null ? post.getCategory().getName() : null;
    }

    public PostResponse(List<Long> ids) {
        this.ids = ids;
    }
}
