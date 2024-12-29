package com.jpacommunity.board.core.repository;


import com.jpacommunity.board.core.entity.Category;

import java.util.List;

public interface CategoryRepositoryCustom {
    List<Category> listSortedWithHierarchy();
    List<Category> listSortedRootCategories();
    Category getWithSortedChildrenById(Long id);
}