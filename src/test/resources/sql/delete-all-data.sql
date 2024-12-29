-- 1. 외래 키 제약 조건 비활성화
SET FOREIGN_KEY_CHECKS = 0;

-- 2. categories 테이블의 모든 데이터 삭제
TRUNCATE TABLE p_categories;

-- 3. 외래 키 제약 조건 활성화
SET FOREIGN_KEY_CHECKS = 1;