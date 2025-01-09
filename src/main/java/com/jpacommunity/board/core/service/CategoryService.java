package com.jpacommunity.board.core.service;

import com.jpacommunity.board.api.controller.response.CategoryResponse;
import com.jpacommunity.board.api.dto.CategoryCreateRequest;
import com.jpacommunity.board.api.dto.CategoryUpdateRequest;
import com.jpacommunity.board.core.entity.Category;
import com.jpacommunity.board.core.repository.category.CategoryRepository;
import com.jpacommunity.global.exception.JpaCommunityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.jpacommunity.global.exception.ErrorCode.*;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse create(CategoryCreateRequest categoryCreateRequest) {
        Category categoryEntity = new Category(categoryCreateRequest);

        if (categoryCreateRequest.getParentId() != null) {
            Category parentCategory = fetchById(categoryCreateRequest.getParentId()); // 부모 카테고리 Entity 조회
            Integer depth = categoryCreateRequest.getDepth();
            Integer parentDepth = parentCategory.getDepth();

            if (depth != (parentDepth + 1)) {
                log.warn("부모 카테고리와의 Depth 관계가 잘못 되었습니다.");
                throw new JpaCommunityException(INVALID_PARAMETER);
            }

            categoryEntity.updateOrderIndex(
                    allocateNextOrderIndex(parentCategory) // calculateOrderIndex(parentCategory.getChildren())
            );
            categoryEntity.setParent(parentCategory); // 편의 메서드
        } else {
            // parentId 가 null 인 경우
            Integer depth = categoryCreateRequest.getDepth();

            if (depth > 0) {
                log.warn("부모 카테고리 ID가 비어있습니다.");
                throw new JpaCommunityException(INVALID_PARAMETER);
            }

            categoryEntity.updateOrderIndex(calculateOrderIndex(categoryRepository.findByParentIsNull()));
        }

        Category savedEntity = categoryRepository.save(categoryEntity);
        return new CategoryResponse(savedEntity);
    }

    @Transactional
    public CategoryResponse update(long id, CategoryUpdateRequest categoryUpdateRequest) {
        // 1. 기존 카테고리 조회
        Category category = fetchById(id);

        // 2. 부모의 children 리스트 갯수로 categoryUpdateRequest orderIndex 값 수정 제한
        Category parent = category.getParent(); // 부모 카테고리 가져오기
        if (parent == null) {
            // 최상단 카테고리
            validateRootForReOrderIndex(categoryUpdateRequest);
        } else {
            // 그 외
            validateSubForReOrderIndex(parent, categoryUpdateRequest);
        }

        // 3. orderIndex 중복 확인
        Category targetCategory = categoryRepository.findByParentIdAndOrderIndex(
                category.getCategoryParentId(), // null 처리 되어 있음
                categoryUpdateRequest.getOrderIndex()
        ).orElse(null);

        if (targetCategory != null) {
            // 변경 순번(orderIndex)과 동일한 카테고리가 존재
            category.updateName(categoryUpdateRequest.getName());
            category.updateLink(categoryUpdateRequest.getLink());
            swapOrderIndex(category, targetCategory);
        } else {
            // 이름 및 순번 변경
            category.updateName(categoryUpdateRequest.getName());
            category.updateLink(categoryUpdateRequest.getLink());
            category.updateOrderIndex(categoryUpdateRequest.getOrderIndex());
        }

        // 4. 수정된 결과를 DTO로 변환 후 반환
        return new CategoryResponse(category);
    }

    private void swapOrderIndex(Category sourceCategory, Category targetCategory) {
        // orderIndex 교환용 변수
        int swapOrderIndex = targetCategory.getOrderIndex();
        targetCategory.updateOrderIndex(sourceCategory.getOrderIndex());
        sourceCategory.updateOrderIndex(swapOrderIndex);
    }

    @Transactional
    public CategoryResponse delete(long id) {
        // 1. 삭제할 엔티티를 조회
        Category category = categoryRepository.getWithSortedChildrenById(id);

        // 연관관계 편의 메서드. 카테고리 순번 재정렬
        if (category.getParent() == null) {
            // 최상위 카테고리 순번 재정렬
            List<Category> topLevelCategories = categoryRepository.findByParentIsNull(); // 최상위 카테고리 목록 조회

            topLevelCategories.remove(category); // 삭제 대상 제거
            reorderTopLevelCategories(topLevelCategories); // 카테고리의 orderIndex 재정렬
        } else {
            // 부모가 있는 경우 자식 순번 재정렬
            category.getParent().removeChild(category);
            reorderChildren(category.getParent());
        }

        // 2. 하위 카테고리 존재 여부 확인
        if (categoryRepository.existsByParentId(id)) {
            log.debug("하위 카테고리 존재하여 삭제가 불가능 합니다. id : {}", id);
            throw new JpaCommunityException(RESOURCE_CONFLICT);
        }

        // 3. DTO 생성 (삭제된 정보 반환)
        CategoryResponse response = new CategoryResponse(category);

        // 4. 삭제
        categoryRepository.delete(category);

        // 5. 삭제된 DTO 반환
        return response;
    }

    // CategoryResponse 로 변환하여 반환
    public CategoryResponse getById(Long id) {
        return new CategoryResponse(fetchById(id));
    }

    // JPQL
    public CategoryResponse getWithSortedChildrenById(Long id) {
        return new CategoryResponse(categoryRepository.getWithSortedChildrenById(id));
    }

    // JPQL
    public List<CategoryResponse> getRootCategories() {
        final List<Category> all = categoryRepository.listSortedRootCategories();
        return all.stream().map(CategoryResponse::fromWithoutChildren).collect(Collectors.toList());
    }

    // JPQL
    public List<CategoryResponse> getAllCategories() {
        final List<Category> all = categoryRepository.listSortedWithHierarchy();
        return all.stream().map(CategoryResponse::new).collect(Collectors.toList());
    }

    /**
     * 최상단 카테고리 카테고리 변경에 대한 유효성 검증
     *
     * @param categoryUpdateRequest 카테고리 변경 정보
     * @throws JpaCommunityException
     */
    public void validateRootForReOrderIndex(CategoryUpdateRequest categoryUpdateRequest) throws JpaCommunityException {
        List<Category> rootCategories = categoryRepository.findByParentIsNull(); // 최상단 카테고리 목록
        int rootCategoriesCount = rootCategories.size(); // 최상단 카테고리 개수
        Integer orderIndex = categoryUpdateRequest.getOrderIndex();

        // orderIndex가 최상단 카테고리 수보다 많거나 같으면 예외 발생
        if (orderIndex > rootCategoriesCount) {
            log.error("orderIndex는 최상단 카테고리의 수보다 작아야 합니다.");
            throw new JpaCommunityException(INVALID_PARAMETER);
        }
    }

    /**
     * 최상단 카테고리가 아닌 그 외 카테고리 변경에 대한 유효성 검증
     *
     * @param parent 부모 카테고리
     * @param categoryUpdateRequest 카테고리 변경 정보
     * @throws JpaCommunityException
     */
    public void validateSubForReOrderIndex(Category parent, CategoryUpdateRequest categoryUpdateRequest) throws JpaCommunityException {
        // 그 외
        List<Category> siblings = parent.getChildren(); // 부모의 자식 리스트
        int siblingsCount = siblings.size(); // 형제 카테고리(자식)의 개수
        Integer orderIndex = categoryUpdateRequest.getOrderIndex();

        // orderIndex가 형제 카테고리 수보다 많거나 같으면 예외 발생
        if (orderIndex > siblingsCount) {
            log.error("orderIndex는 부모 카테고리의 자식 수보다 작아야 합니다.");
            throw new JpaCommunityException(INVALID_PARAMETER);
        }
    }

    /**
     * 최상위 카테고리 순번 재정렬
     *
     * @param topLevelCategories 최상위 카테고리 목록
     */
    public void reorderTopLevelCategories(List<Category> topLevelCategories) {
        for (int i = 0; i < topLevelCategories.size(); i++) {
            topLevelCategories.get(i).updateOrderIndex(i + 1);
        }
    }

    /**
     * 카테고리 순번을 설정
     * @param parent 부모 카테고리
     */
    public int allocateNextOrderIndex(Category parent) {
        return calculateOrderIndex(parent.getChildren());
    }

    /**
     * 카테고리 목록의 갯수로 부터 다음 순번을 계산
     *
     * @param list 카테고리 목록
     * @return 다음 카테고리 순번
     */
    private int calculateOrderIndex(List<?> list) {
        return (list == null) ? 1 : list.size() + 1;
    }

    /**
     * 카테고리 순번을 사이즈에 맞게 재정렬한다.
     *
     * @param category 카테고리
     */
    private void reorderChildren(Category category) {
        for (int i = 0; i < category.getChildren().size(); i++) {
            category.getChildren().get(i).updateOrderIndex(i + 1);
        }
    }

    // 공통 메서드: Category 엔티티를 반환
    public Category fetchById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new JpaCommunityException(RESOURCE_NOT_FOUND, "id -> " + id + " 는 존재하지 않는 카테고리 입니다"));
    }

    private Category fetchByParentIdAndOrderIndex(Long parentId, int orderIndex) {
        return categoryRepository.findByParentIdAndOrderIndex(parentId, orderIndex)
                .orElseThrow(() -> new JpaCommunityException(RESOURCE_NOT_FOUND, "parentId -> " + parentId + ", orderIndex -> " + orderIndex + " 는 존재하지 않는 카테고리 입니다"));
    }
}