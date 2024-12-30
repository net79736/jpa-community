package com.jpacommunity.board.repository.querydsl;

import com.jpacommunity.board.api.controller.response.CategoryResponse;
import com.jpacommunity.board.core.entity.Category;
import com.jpacommunity.board.core.repository.CategoryQuerydslRepository;
import com.jpacommunity.board.core.repository.CategoryRepository;
import com.jpacommunity.board.core.service.CategoryService;
import com.jpacommunity.global.exception.ErrorCode;
import com.jpacommunity.global.exception.JpaCommunityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@SqlGroup({
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/category-insert-date.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
})
@DisplayName("카테고리 테스트 2")
class CategoryQuerydslRepositoryTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private CategoryQuerydslRepository repository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;
    /**
     SELECT c1_0.id,
            c2_0.parent_id,
            c2_0.id,
            c2_0.depth,
            c2_0.NAME,
            c2_0.order_index,
            c1_0.depth,
            c1_0.NAME,
            c1_0.order_index,
            c1_0.parent_id
     FROM   categories c1_0
     LEFT JOIN categories c2_0
     ON c1_0.id = c2_0.parent_id
     WHERE  c1_0.parent_id IS NULL
     ORDER  BY c1_0.order_index, c2_0.order_index
     */
    @Test
    @DisplayName("카테고리 세프조인 테스트")
    public void querydsl_self_join_select_test() throws Exception {
        // given
        List<Category> categories = repository.listSortedWithHierarchy();

        // when
        for (Category category : categories) {
            System.out.println(category);
        }

        // then
    }

    /**
     SELECT c1_0.id,
            c1_0.depth,
            c1_0.NAME,
            c1_0.order_index,
            c1_0.parent_id
     FROM   categories c1_0
     WHERE  c1_0.parent_id IS NULL
     ORDER  BY c1_0.order_index
     */
    @Test
    @DisplayName("카테고리 단일 테스트")
    public void querydsl_normal_select_test() throws Exception {
        // given
        List<Category> categories = repository.listSortedRootCategories();

        // when
        for (Category category : categories) {
            System.out.println(category);
        }

        // then
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    @DisplayName("부모 카테고리의 갯수가 맞는지 테스트")
    public void category_root_category_return_value_test() throws Exception {
        // given
        List<Category> categories = categoryRepository.listByParentIsNull();

        // when
        // then
        assertThat(categories.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("List 형태의 Repository 함수에서 부모 ID 를 이상하게 조회 했을 때 에러를 반환하는지 또는 Null 을 반환하는지 테스트")
    public void category_jpa_list_return_value_test() throws Exception {
        // given
        // when
        // then
        JpaCommunityException jpaCommunicationException = assertThrows(JpaCommunityException.class, () -> categoryRepository.getByParentIdAndOrderIndex(111L, 2));

        String message = jpaCommunicationException.getMessage();
        ErrorCode errorCode = jpaCommunicationException.getErrorCode();
        assertThat(message).isEqualTo("parentId -> 111, orderIndex -> 2 는 존재하지 않는 카테고리 입니다");
        assertThat(errorCode).isEqualTo(jpaCommunicationException.getErrorCode());
    }

    @Test
    @DisplayName("Querydsl 메서드를 테스트한다.")
    public void querydsl_method_test() throws Exception {
        // given
        // when
        List<CategoryResponse> allCategories = categoryService.getAllCategories();

        // then
        System.out.println(allCategories.size());
    }
}