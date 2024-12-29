package com.jpacommunity.board.core.entity;

import com.jpacommunity.board.api.dto.CategoryCreateRequest;
import com.jpacommunity.common.domain.Base;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_categories")
public class Category extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer depth;

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    private String link;

    @Builder
    public Category(CategoryCreateRequest categoryCreateRequest) {
        this.name = categoryCreateRequest.getName();
        this.depth = categoryCreateRequest.getDepth();
        this.link = categoryCreateRequest.getLink();
    }

    /**
     * 카테고리의 부모를 변경
     * @param parent 부모 카테고리
     */
    public void setParent(Category parent) {
        this.parent = parent;
        parent.getChildren().add(this);
    }

    /**
     * 카테고리의 이름을 변경
     * @param name 카테고리 명
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 카테고리 순번을 변경
     * @param orderIndex 순번
     */
    public void updateOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    /**
     * 카테고리 링크를 변경
     * @param link 링크
     */
    public void updateLink(String link) {
        this.link = link;
    }

    // 삭제 로직: 자식 삭제 및 orderIndex 재정렬
    public void removeChild(Category child) {
        this.children.remove(child);
    }

    /**
     * 부모 카테고리의 ID 를 반환
     * 부모가 없으면 null 을 반환
     *
     * @return 부모 카테고리 ID 또는 null
     */
    public Long getCategoryParentId() {
        return this.parent != null ? this.parent.getId() : null;
    }
}
