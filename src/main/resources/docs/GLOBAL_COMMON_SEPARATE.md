# global과 common 패키지의 주요 차이

| 구분    | global                               | common                              |
|-------|--------------------------------------|-------------------------------------|
| 목적    | 프로젝트 전역에 적용되는 로직        | 여러 도메인에서 공통적으로 사용되는 로직 |
| 포함 요소 | 설정, 보안, 전역 유틸리티, 전역 예외 처리 | 공통 엔티티, 공통 유틸리티, 공통 예외 클래스 |
| 적용 범위 | 애플리케이션 전체                   | 특정 도메인을 제외한 비즈니스 전반     |
| 예시 클래스 | SecurityConfig, JwtProvider, GlobalException | BaseEntity, PasswordUtil, ValidationAdvice |

## 왜 이렇게 나눠야 할까?
1. 책임 분리:
 * global 은 **전역적인 설정 및 보안**처럼 애플리케이션 전체의 인프라 로직에 가까운 코드를 포함.
 * common 은 **여러 도메인에서 재사용 가능한 비즈니스 독립적인 로직**을 포함.

2. 확장성:
 * **새로운 설정(DatabaseConfig, CacheConfig)이 추가될 때는 global 패키지에 추가.**
 * **새로운 유틸리티**나 공통 예외 클래스는 common에 추가.

3. 가독성:
 * 패키지가 명확히 나뉘어져 있으면, "이 클래스는 설정 관련인가?", "공통 유틸리티인가?"를 직관적으로 파악 가능.

## 결론: global과 common은 왜 필요한가?
global은 "애플리케이션 설정"과 관련된 모든 전역적인 코드를 위한 것:
 * 예: SecurityConfig, JwtProvider, GlobalException.

common은 "재사용 가능한 공통 로직"을 위한 것:
 * 예: BaseEntity, PasswordUtil, ResponseDto.
