https://chaewsscode.tistory.com/233

하지만 커스텀 필터(ex. JwtAuthenticationFilter)를 @Component나 @Bean 어노테이션을 사용해 등록했다면,
@WebSecurityCustomizer의 web.ignoring()이 적용되지 않는다.
이는 필터 체인의 구성 순서와 커스텀 필터의 등록 방식 때문인데 web.ignoring() 설정으로 특정 경로를 무시하더라도,
커스텀 필터는 보안 필터 체인에 추가된 이상 **무시된 경로를 포함한 모든 요청에 대해 필터링을 수행**한다.

**Security Filter Chain** 은 Spring Security 에서 **HTTP 요청**의 다양한 보안 기능을 제공하기
위한 여러 종류의 필터들의 모음이다.
**기본적으로 제공하는 필터**들이 있고,
**사용자가 만든 커스텀 필터**도 **필터 체인으로 등록**하여 사용할 수 있다.

각 필터는 특정한 보안 작업을 수행하고 다음의 필터로 요청을 전달하는데,
**FilterChainProxy 를 통해 필터 체인이 관리**되며 요청이 적절한 필터로 전달된다.
