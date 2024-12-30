package com.jpacommunity.board.service;

import com.jpacommunity.board.api.controller.response.CategoryResponse;
import com.jpacommunity.board.api.dto.CategoryCreateRequest;
import com.jpacommunity.board.api.dto.CategoryUpdateRequest;
import com.jpacommunity.board.core.entity.Category;
import com.jpacommunity.board.core.repository.CategoryJpaRepository;
import com.jpacommunity.board.core.repository.CategoryRepository;
import com.jpacommunity.board.core.service.CategoryService;
import com.jpacommunity.global.exception.ErrorCode;
import com.jpacommunity.global.exception.JpaCommunityException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jpacommunity.global.exception.ErrorCode.INVALID_PARAMETER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@SqlGroup({
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/category-insert-date.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
})
@DisplayName("카테고리 테스트 1")
class CategoryServiceTest {

    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CategoryJpaRepository categoryJpaRepository;

    @Autowired
    EntityManager entityManager;

    private Map<String, Long> categoryIds = new HashMap<>();

    @BeforeEach
    public void init() {
//        insertMainCategories();
//        insertMiddleCategories();
//        insertSubCategories();

        // 삽입된 카테고리 수 출력
        System.out.println("전체 카테고리 생성 갯수: " + categoryIds.size());

        // 간단한 검증
        // assertThat(categoryIds).isNotEmpty();
        // assertThat(categoryIds).containsKey("축구");
        // assertThat(categoryIds).containsKey("농구");
        // assertThat(categoryIds).containsKey("야구");
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    @DisplayName("하위 카테고리가 존재하는 경우 삭제할 수 없다.")
    public void delete_test_1() throws Exception {
        // given
        Long deleteId = 4L;

        // when
        // then
        JpaCommunityException invalidOperationException = assertThrows(JpaCommunityException.class, () -> categoryService.delete(deleteId));
        assertThat(invalidOperationException.getMessage()).isEqualTo("Resource is in a state that prevents this operation");
    }

    @Test
    @DisplayName("존재하지 않는 카테고리를 삭제하는 경우 에러를 발생시킨다.")
    public void delete_test_2() throws Exception {
        // given
        Long deleteId = 99L;

        // when
        // then
        JpaCommunityException resourceNotFoundException = assertThrows(JpaCommunityException.class, () -> categoryService.delete(deleteId));
        assertThat(resourceNotFoundException.getMessage()).isEqualTo("Resource not found");
    }

    @Test
    @DisplayName("중간 순번의 카테고리 삭제 시 순번이 제졍렬 된다")
    public void delete_test_3() throws Exception {
        // given
        Long deleteId = 17L; // 중간 순번의 카테고리 번호

        /**
         * '25','2','자유','1','5'
         * '26','2','유머','2','5'
         * '27','2','질문','3','5'
         * '28','2','영상','4','5'
         * '29','2','사건 사고','5','5'
         * '30','2','전적 인증','6','5'
         * '31','2','팬 아트','7','5'
         */
        // when
        CategoryResponse deleteResponse = categoryService.delete(deleteId);
        Long parentId = deleteResponse.getParentId();

        CategoryResponse byIdWithQuerydslOrThrow = categoryService.getWithSortedChildrenById(parentId);

        // then
        List<CategoryResponse> children = byIdWithQuerydslOrThrow.getChildren();

        // 검증: 삭제된 ID가 자식 목록에 존재하지 않는지 확인
        assertThat(children).noneMatch(child -> child.getId().equals(deleteId));

        // 검증: 순번(orderIndex)이 연속적으로 정렬되었는지 확인
        for (int i = 0; i < children.size(); i++) {
            System.out.println(children.get(i).getName() + " -> " + children.get(i).getOrderIndex());
            assertThat(children.get(i).getOrderIndex()).isEqualTo(i + 1);
        }
    }

    @Test
    @DisplayName("최상위 카테고리는 삭제 시 순번 재정렬 테스트")
    @Disabled
    public void delete_test_4() throws Exception {
        // given
        Long deleteId = 2L; // 삭제할 최상위 카테고리 ID ("농구")

        // 삭제 전 상태: 최상위 카테고리 목록
        List<Category> beforeDelete = categoryRepository.listByParentIsNull();
        System.out.println("삭제 전 상태:");
        beforeDelete.forEach(category ->
                System.out.println(category.getName() + " -> " + category.getOrderIndex())
        );

        // when
        categoryService.delete(deleteId);

        // then
        // 삭제 후 상태: 최상위 카테고리 목록
        List<Category> afterDelete = categoryRepository.listByParentIsNull();
        System.out.println("\n삭제 후 상태:");
        afterDelete.forEach(category ->
                System.out.println(category.getName() + " -> " + category.getOrderIndex())
        );

        // 검증: 삭제된 ID가 존재하지 않는지 확인
        assertThat(afterDelete).noneMatch(category -> category.getId().equals(deleteId));

        // 검증: 순번(orderIndex)이 연속적으로 정렬되었는지 확인
        for (int i = 0; i < afterDelete.size(); i++) {
            assertThat(afterDelete.get(i).getOrderIndex()).isEqualTo(i + 1);
        }
    }

    @Test
    @DisplayName("카테고리 삭제 테스트")
    public void delete_test_5() throws Exception {
        // given
        Long deleteId = 25L;

        // when
        // then
        CategoryResponse deleteResponse = categoryService.delete(deleteId);
        assertThat(deleteResponse.getName()).isEqualTo("기타 리그");
        assertThat(deleteResponse.getParentId()).isEqualTo(6L);
        assertThat(deleteResponse.getId()).isEqualTo(deleteId);
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    @DisplayName("존재하지 않는 카테고리를 수정하는 경우 에러를 발생시킨다.")
    public void update_test_1() throws Exception {
        // given
        Long deleteId = 99L;
        CategoryUpdateRequest categoryRequest = CategoryUpdateRequest.builder()
                .name("바뀌어 버린 카테고리 이름")
                .orderIndex(10002)
                .build();

        // when
        // then
        JpaCommunityException resourceNotFoundException = assertThrows(JpaCommunityException.class, () -> categoryService.update(deleteId, categoryRequest));
        assertThat(resourceNotFoundException.getMessage()).isEqualTo("id -> 99 는 존재하지 않는 카테고리 입니다");
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    @Disabled
    @DisplayName("카테고리 수정시 필수 값 테스트 (orderIndex)")
    public void update_test_2() throws Exception {
        // given
        Long existingCategoryId = 4L; // 기존에 존재하는 카테고리 ID
        CategoryUpdateRequest categoryRequest = CategoryUpdateRequest.builder()
                .orderIndex(null)
                .build();

        // when
        // then
        JpaCommunityException generalApiException = assertThrows(JpaCommunityException.class, () -> categoryService.update(existingCategoryId, categoryRequest));
        assertThat(generalApiException.getMessage()).isEqualTo("orderIndex 는 필수 값입니다.");
    }

    @Test
    @DisplayName("카테고리 수정 시 이미 존재하는 순번이 있는 경우 순번 swap 이 이루어지는지 체크한다.")
    public void update_test_3() throws Exception {
        // given
        Long existingCategoryId = 4L; // 기존에 존재하는 카테고리 ID
        int updateOrderIndex = 3; // 변경하려는 orderIndex
        CategoryUpdateRequest categoryRequest = CategoryUpdateRequest.builder()
                .orderIndex(updateOrderIndex)
                // .parentId(1L)
                .build();

        // when
        CategoryResponse updatedCategory = categoryService.update(existingCategoryId, categoryRequest);

        // then
        assertThat(updatedCategory).isNotNull();
        assertThat(updatedCategory.getOrderIndex()).isEqualTo(updateOrderIndex);

        // 추가 검증: 이전 orderIndex를 가진 카테고리의 순번이 교환되었는지 확인
        CategoryResponse swappedCategory = categoryService.getById(6L); // 원래 3번 순번이었던 카테고리
        assertThat(swappedCategory.getOrderIndex()).isEqualTo(1); // 1번으로 변경됨
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    @DisplayName("카테고리 수정 테스트")
    public void update_test_4() throws Exception {
        // given
        Long deleteId = 2L;
        CategoryUpdateRequest categoryRequest = CategoryUpdateRequest.builder()
                .orderIndex(2)
                .name("농구")
                .build();

        // when
        // then
        CategoryResponse updateResponse = categoryService.update(deleteId, categoryRequest);
        assertThat(updateResponse.getName()).isEqualTo("농구");
        assertThat(updateResponse.getId()).isEqualTo(deleteId);
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    @DisplayName("새로운 카테고리(대)를 생성한다.")
    public void create_level_one_test() throws Exception {
        // given
        CategoryCreateRequest categoryCreate = CategoryCreateRequest.builder()
                .name("4. 아이스 하키")
                .depth(0)
                .build();

        // when
        CategoryResponse categoryResponse = categoryService.create(categoryCreate);

        // then
        assertThat(categoryResponse.getName()).isEqualTo("4. 아이스 하키");
    }

    @Test
    @DisplayName("새로운 카테고리(중)를 생성한다.")
    public void create_level_two_test() throws Exception {
        // given 59 60 61
        CategoryCreateRequest categoryCreate = CategoryCreateRequest.builder()
                .name("2.2 e스포츠2")
                .depth(1)
                .parentId(1L)
                .build();

        // when
        CategoryResponse categoryResponse = categoryService.create(categoryCreate);

        // then
        assertThat(categoryResponse.getName()).isEqualTo("2.2 e스포츠2");
    }

    @Test
    @DisplayName("새로운 카테고리(소)를 생성한다.")
    public void create_level_three_test() throws Exception {
        // given
        CategoryCreateRequest categoryCreate = CategoryCreateRequest.builder()
                .name("e 스포츠 LOL")
                .depth(2)
                .parentId(7L) // e 스포츠의 하위 카테고리 생성
                .build();

        // when
        CategoryResponse categoryResponse = categoryService.create(categoryCreate);

        // then
        assertThat(categoryResponse.getName()).isEqualTo("e 스포츠 LOL");
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    @DisplayName("depth 가 NULL 로 들어온 경우 테스트")
    @Disabled
    public void depth_save_test_1() throws Exception {
        // given
        CategoryCreateRequest categoryCreate = CategoryCreateRequest.builder()
                .name("새로운 카테고리 테스트")
                .depth(null)
                .parentId(7L) // e 스포츠의 하위 카테고리 생성
                .build();

        // when
        assertThrows(JpaCommunityException.class, () -> categoryService.create(categoryCreate));

        // then
    }

    @Test
    @DisplayName("depth 가 0 이상인데 parentId 가 없는 경우 테스트 로 들어온 경우 테스트")
    public void depth_save_test_2() throws Exception {
        // given
        CategoryCreateRequest categoryCreate = CategoryCreateRequest.builder()
                .name("새로운 카테고리 테스트")
                .depth(3)
                .parentId(null) // 부모 카테고리 널로 설정
                .build();

        // when
        // then
        JpaCommunityException jpaCommunicationException = assertThrows(JpaCommunityException.class, () -> categoryService.create(categoryCreate));
        String message = jpaCommunicationException.getMessage();
        ErrorCode errorCode = jpaCommunicationException.getErrorCode();
        assertThat(message).isEqualTo("Invalid parameter value");
        assertThat(errorCode).isEqualTo(INVALID_PARAMETER);
    }

    @Test
    @DisplayName("categoryCreate 의 depth 는 항상 부모카테고리 depth 의 + 1 의 값이어야 한다는 테스트")
    public void depth_save_test_3() throws Exception {
        // given
        CategoryCreateRequest categoryCreate = CategoryCreateRequest.builder()
                .name("새로운 카테고리 테스트")
                .depth(4)
                .parentId(15L) // 부모 카테고리 15L(depth = 2)
                .build();
        // when
        // then
        JpaCommunityException jpaCommunicationException = assertThrows(JpaCommunityException.class, () -> categoryService.create(categoryCreate));
        String message = jpaCommunicationException.getMessage();
        ErrorCode errorCode = jpaCommunicationException.getErrorCode();
        assertThat(message).isEqualTo("Invalid parameter value");
        assertThat(errorCode).isEqualTo(INVALID_PARAMETER);
    }

//    private void insertMainCategories() {
//        String[] mainCategories = {"축구", "농구", "야구"};
//        for (int i = 0; i < mainCategories.length; i++) {
//            CategoryCreateRequest categoryCreate = CategoryCreateRequest.builder()
//                    .name(mainCategories[i])
//                    .depth(0)
//                    .parentId(null)
//                    .orderIndex(i + 1)
//                    .build();
//            CategoryResponse categoryResponse = categoryService.create(categoryCreate);
//            categoryIds.put(categoryResponse.getName(), categoryResponse.getId());
//        }
//    }
//
//    private void insertMiddleCategories() {
//        String[] mainCategories = {"축구", "농구", "야구"};
//        String[] middleCategories = {"정보", "커뮤니티", "e스포츠"};
//
//        for (String main : mainCategories) {
//            for (int i = 0; i < middleCategories.length; i++) {
//                CategoryCreateRequest categoryCreate = CategoryCreateRequest.builder()
//                        .name(middleCategories[i])
//                        .depth(1)
//                        .parentId(categoryIds.get(main))
//                        .orderIndex(i + 1)
//                        .build();
//                CategoryResponse categoryResponse = categoryService.create(categoryCreate);
//                categoryIds.put(main + "-" + categoryResponse.getName(), categoryResponse.getId());
//            }
//        }
//    }
//
//    private void insertSubCategories() {
//        insertInfoSubCategories();
//        insertCommunitySubCategories();
//        insertEsportsSubCategories();
//    }
//
//    private void insertInfoSubCategories() {
//        String[] mainCategories = {"축구", "농구", "야구"};
//        String[] subCategories = {"OP.GG 기획", "유저 뉴스", "팁과 노하우", "패치노트"};
//
//        for (String main : mainCategories) {
//            for (int i = 0; i < subCategories.length; i++) {
//                CategoryCreateRequest categoryCreate = CategoryCreateRequest.builder()
//                        .name(subCategories[i])
//                        .depth(2)
//                        .parentId(categoryIds.get(main + "-정보"))
//                        .orderIndex(i + 1)
//                        .build();
//                CategoryResponse categoryResponse = categoryService.create(categoryCreate);
//                categoryIds.put(categoryResponse.getName(), categoryResponse.getId());
//            }
//        }
//    }
//
//    private void insertCommunitySubCategories() {
//        String[] mainCategories = {"축구", "농구", "야구"};
//        String[] subCategories = {"자유", "유머", "질문", "영상", "사건 사고", "전적 인증", "팬 아트"};
//
//        for (String main : mainCategories) {
//            for (int i = 0; i < subCategories.length; i++) {
//                String name = main.equals("축구") ? subCategories[i] : subCategories[i] + " (" + main + ")";
//                CategoryCreateRequest categoryCreate = CategoryCreateRequest.builder()
//                        .name(name)
//                        .depth(2)
//                        .parentId(categoryIds.get(main + "-커뮤니티"))
//                        .orderIndex(i + 1)
//                        .build();
//                CategoryResponse categoryResponse = categoryService.create(categoryCreate);
//                categoryIds.put(main + "-커뮤니티-" + name, categoryResponse.getId());
//            }
//        }
//    }
//
//    private void insertEsportsSubCategories() {
//        String[] mainCategories = {"축구", "농구", "야구"};
//        String[] subCategories = {"LCK", "기타 리그"};
//
//        for (String main : mainCategories) {
//            for (int i = 0; i < subCategories.length; i++) {
//                String name = main.equals("축구") ? subCategories[i] : subCategories[i] + " (" + main + ")";
//                CategoryCreateRequest categoryCreate = CategoryCreateRequest.builder()
//                        .name(name)
//                        .depth(2)
//                        .parentId(categoryIds.get(main + "-e스포츠"))
//                        .orderIndex(i + 1)
//                        .build();
//                CategoryResponse categoryResponse = categoryService.create(categoryCreate);
//                categoryIds.put(main + "-e스포츠-" + name, categoryResponse.getId());
//            }
//        }
//    }
}