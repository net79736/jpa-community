-- 최상위 카테고리: 축구, 농구, 야구
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (1, '축구', 0, NULL, 1);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (2, '농구', 0, NULL, 2);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (3, '야구', 0, NULL, 3);

-- 축구의 하위 카테고리
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (4, '정보', 1, 1, 1);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (5, '커뮤니티', 1, 1, 2);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (6, 'e스포츠', 1, 1, 3);

-- 농구의 하위 카테고리
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (7, '정보', 1, 2, 1);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (8, '커뮤니티', 1, 2, 2);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (9, 'e스포츠', 1, 2, 3);

-- 야구의 하위 카테고리
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (10, '정보', 1, 3, 1);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (11, '커뮤니티', 1, 3, 2);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (12, 'e스포츠', 1, 3, 3);

-- 축구 > 정보의 하위 카테고리
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (13, 'OP.GG 기획', 2, 4, 1);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (14, '유저 뉴스', 2, 4, 2);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (15, '팁과 노하우', 2, 4, 3);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (16, '패치노트', 2, 4, 4);

-- 축구 > 커뮤니티의 하위 카테고리
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (17, '자유', 2, 5, 1);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (18, '유머', 2, 5, 2);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (19, '질문', 2, 5, 3);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (20, '영상', 2, 5, 4);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (21, '사건 사고', 2, 5, 5);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (22, '전적 인증', 2, 5, 6);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (23, '팬 아트', 2, 5, 7);

-- 축구 > e스포츠의 하위 카테고리
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (24, 'LCK', 2, 6, 1);
INSERT INTO p_categories (id, name, depth, parent_id, order_index) VALUES (25, '기타 리그', 2, 6, 2);
