package com.jpacommunity.board.core.repository;

import com.jpacommunity.board.core.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<Category> listByParentIsNull();
    List<Category> listSortedByParentIsNull();
    Optional<Category> findByParentIdAndOrderIndex(Long parentId, int orderIndex);
    Category getByParentIdAndOrderIndex(Long parentId, int orderIndex);
    Optional<Category> findById(long id);
    Category getById(long id);
}
