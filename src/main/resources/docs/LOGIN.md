### 1. 로컬 사용자 로그인 순서도
```mermaid
graph TD
    A((로컬 사용자 로그인 시도)) --> B[로그인 인증]
    B -->|성공| C{사용자 상태 확인}
    B -->|실패| D[401 에러 반환]
    C -->|ACTIVE| E[JWT 생성]
    C -->|ACTIVE가 아님| F[PENDING 인 경우: 423 에러 반환\nINACTIVE 인 경우 : 403 에러 반환
    PENDING 상태인 경우 프론트 단에서 상태 변경 및 토큰 재발급 API 사용하여 로그인 진행]
    E --> G((응답 완료))
```

### 2. 소셜 로그인 순서도
```mermaid
graph TD
    A((소셜 로그인 시도)) --> B[로그인 인증]
    B -->|성공| C{사용자 상태 확인}
    B -->|실패| D[소셜 로그인 프로세스를 따름]
    C -->|ACTIVE| E[JWT 생성]
    C -->|ACTIVE가 아님| F[PENDING 상태인 경우 프론트 단에서 상태 변경 및 토큰 재발급 API 사용하여 로그인 진행]
    F --> G[쿼리스트링에 상태 코드 전달\n ex: http://localhost:3000?status=PENDING]
    E --> H((응답 완료))
```

### 3. 계정을 관리자 권한으로 변경하고 싶은 경우
```mermaid
graph TD
    A((권한 변경 방법\n ADMIN, USER)) --> B[사전에 환경 변수 등록 필요\n PLAYHIVE_CLIENT_ID 와 PLAYHIVE_CLIENT_SECRET\n 보안용 코드 값\n]
    B --> C[최초 회원가입 후 상태: PENDING, 권한: USER]
    C --> D[권한 변경 API 호출]
    D --> E{clientId, secretKey 유효성 확인}
    E -->|유효함| F[관리자 권한 부여 조건 충족]
    E -->|유효하지 않음| G[에러 반환]
    F --> I{사용자 존재 확인}
    I -->|존재| J[권한 변경]
    I -->|존재하지 않는 사용자| G[에러 반환]
    J --> K((응답 완료))
```

### 4. PLAYHIVE_CLIENT_ID 와 PLAYHIVE_CLIENT_SECRET 값
PLAYHIVE_CLIENT_ID: 1ZDYmrYNwVcioFZNKVQ5VSqylF
PLAYHIVE_CLIENT_SECRET: RK1j0C33CUexMs

### 5. 로그인 절차 없이 토큰 발급 방법
Swagger UI 에서 /api/members/get-token/user/{email}[GET] 해당 메서드를 사용하여 토큰 발급이 가능합니다.
해당 기능은 개발 편의 기능으로 만들어 졌습니다.
