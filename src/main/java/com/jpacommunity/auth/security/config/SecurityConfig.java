package com.jpacommunity.auth.security.config;

import com.jpacommunity.auth.oauth2.handler.OAuth2LoginFailureHandler;
import com.jpacommunity.auth.oauth2.service.CustomOAuth2UserService;
import com.jpacommunity.auth.security.filter.AuthenticationEntryPointHandler;
import com.jpacommunity.auth.security.filter.CustomAccessDeniedHandler;
import com.jpacommunity.auth.security.filter.JwtAuthenticationFilter;
import com.jpacommunity.auth.security.filter.TokenAuthenticationFilter;
import com.jpacommunity.jwt.repository.RefreshJpaRepository;
import com.jpacommunity.jwt.util.JwtProvider;
import com.jpacommunity.oauth2.handler.CustomOauth2SuccessHandler;
import com.jpacommunity.security.handler.LogoutSuccessHandler;
import com.jpacommunity.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.HstsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static com.jpacommunity.jwt.controller.ReIssueController.TOKEN_REISSUE_PATH;
import static com.jpacommunity.jwt.util.JwtProvider.HEADER_AUTHORIZATION;
import static com.jpacommunity.jwt.util.JwtProvider.REFRESH_TOKEN_KEY;
import static com.jpacommunity.member.domain.MemberRole.ADMIN;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${FRONT_URL:http://localhost:3000}")
    private String frontUrl;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOauth2SuccessHandler customOauth2SuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final RefreshJpaRepository refreshJpaRepository;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.debug("BCryptPasswordEncoder 빈 등록됨");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // HTTP 헤더 설정
            .headers(headers ->
                    headers
                            .httpStrictTransportSecurity(HstsConfig::disable) // HSTS 비활성화
                            .frameOptions(FrameOptionsConfig::disable)        // FrameOptions 비활성화
            );

        // 로그아웃 비활성화
        http
            .logout((auth) -> auth.disable());

        // csrf disable
        http
            .csrf((auth) -> auth.disable());

        // From 로그인 방식 disable
        http
            .formLogin((auth) -> auth.disable());

        //HTTP Basic 인증 방식 disable
        http
            .httpBasic((auth) -> auth.disable());


        /**
         * JWT를 통한 인증/인가를 위해서 세션을 STATELESS 상태로 설정하는 것이 중요하다.
         *
         * STATELESS: 세션을 서버에서 유지하지 않습니다. 클라이언트가 요청할 때마다 필요한 인증 정보를 요청과 함께 보내야 합니다. 서버는 상태를 유지하지 않으며, 요청이 독립적으로 처리됩니다.
         * STATEFUL: 세션을 서버에서 관리합니다. 클라이언트가 로그인하면 세션이 생성되어 서버에 저장됩니다. 이후 요청은 이 세션을 통해 인증됩니다.
         *
         * # STATELESS 설정의 의미
         * SessionCreationPolicy.STATELESS를 설정하면, 서버가 클라이언트의 세션을 유지하지 않겠다는 뜻입니다. 즉, 서버는 각 요청을 독립적으로 처리하며, 클라이언트는 인증 정보를 매 요청마다 포함해야 합니다.
         * 이 방식은 주로 RESTful API에서 사용되며, 세션 기반 인증 대신 토큰 기반 인증(JWT 등)을 사용하는 경우에 적합합니다.
         */
        //세션 설정 : STATELESS
        http
            .sessionManagement((session) -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
            .oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                            .userService(customOAuth2UserService))
                    .successHandler(customOauth2SuccessHandler)
                    .failureHandler(oAuth2LoginFailureHandler)
            );

        http
            .addFilterAt(
                    new JwtAuthenticationFilter(authenticationManager(), jwtProvider, refreshJpaRepository),
                    UsernamePasswordAuthenticationFilter.class
            ) // 로그인 인증 필터
            .addFilterAfter(
                        new TokenAuthenticationFilter(jwtProvider),
                        JwtAuthenticationFilter.class
            ); // JWT 토큰 검증 필터

        // .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class); // JWTFilter 가 먼저 실행되고 LoginFilter 가 실행됨
        // .addFilterAfter(new JWTFilter(jwtUtil), LoginFilter.class); // LoginFilter 가 먼저 실행되고 JWTFilter 가 실행됨

        // cors 설정
        http
            .cors((corsCustomizer) -> corsCustomizer.configurationSource(configurationSource()));

        // 경로별 인가 작업
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/upload/**").permitAll()       // 정적 자원 접근 허용
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**").permitAll()

                    .requestMatchers("/h2-console").permitAll()       // H2 콘솔 접근 허용
                    .requestMatchers("/api/members/get-token/**").permitAll()       // 테스트용 토큰 발급용
                    .requestMatchers("/api/attachments/**").permitAll()       // 테스트용
                    .requestMatchers("/api/posts/**").permitAll()       // 테스트용

                    .requestMatchers("/api/admin/**").hasAnyAuthority(ADMIN.name())
                    .requestMatchers(HttpMethod.POST, "/api/me/create").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/members/type/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasAnyAuthority(ADMIN.name())
                    .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasAnyAuthority(ADMIN.name())
                    .requestMatchers(HttpMethod.POST, "/api/categories").hasAnyAuthority(ADMIN.name())

                    .requestMatchers(TOKEN_REISSUE_PATH).permitAll()          // 토큰 재발급
                    .requestMatchers("/api/members/role").permitAll()       // 유저 권한 변경 허용

                    .anyRequest().authenticated()                   // 나머지 요청은 모두 허용
            );

        http
            .exceptionHandling(errorHandling ->
                    errorHandling
                        .authenticationEntryPoint(new AuthenticationEntryPointHandler())
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
            );

        // 로그아웃 처리
        http
            .logout(logout -> logout
            .logoutUrl("/logout")
            .invalidateHttpSession(true)
            .logoutSuccessHandler(new LogoutSuccessHandler(jwtProvider, refreshJpaRepository))
            .permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(customUserDetailsService);
        return new ProviderManager(provider);
    }

    /**
     * CORS는 클라이언트가 다른 도메인에 있는 리소스에 접근할 때 브라우저가 이를 안전하게 관리하는 메커니즘입니다.
     * 기본적으로, CORS 요청은 자격 증명을 포함하지 않습니다. 즉, 클라이언트가 보낸 요청에는 쿠키, HTTP 인증 헤더 등이 포함되지 않습니다.
     *
     * 대부분의 경우, 서버는 인증된 사용자만 특정 리소스에 접근할 수 있도록 설정되어 있습니다. 이 경우 쿠키(세션 ID 등)나 토큰(예: JWT)을 통해 사용자를 인증합니다.
     * 클라이언트가 인증된 요청을 보내기 위해서는 이러한 자격 증명이 요청에 포함되어야 합니다.
     *
     * 이 설정을 통해 브라우저는 클라이언트가 요청에 자격 증명을 포함할 수 있도록 허용합니다.
     * 이 설정이 없으면, 브라우저는 쿠키나 인증 헤더를 포함하지 않고 요청을 보냅니다.
     *
     * 즉, setAllowCredentials(true)가 설정되어 있어야 클라이언트가 서버에 요청을 보낼 때 쿠키를 포함할 수 있으며, 이로 인해 세션 유지나 사용자 인증을 할 수 있습니다.
     */
    public CorsConfigurationSource configurationSource() {
        System.out.println("configurationSource cors 설정이 SecurityFilterChain에 등록됨");
        CorsConfiguration configuration = new CorsConfiguration();
        // addAllowedHeader("*")는 클라이언트가 서버에 보낼 수 있는 헤더를 허용하는 것
        // 예: 클라이언트가 Authorization, Content-Type 등의 헤더를 사용해 요청할 수 있습니다.
        //     "*": 모든 헤더를 허용하겠다는 의미입니다.
        configuration.addAllowedHeader("*"); // 클라이언트가 서버에 보낼 수 있는 헤더를 허용하는 것
        configuration.addAllowedMethod("*"); // 허용할 메서드. GET, POST, PUT, DELETE (Javascript 요청 허용)
        // 주의: 보안적으로 민감한 경우 특정 출처만 허용해야 합니다.
        // 예: configuration.addAllowedOriginPattern("http://localhost:3000");
        configuration.addAllowedOrigin(frontUrl); // 모든 IP 주소 허용 (프론트 앤드 IP만 허용 react)
        configuration.setAllowCredentials(true);  // 클라이언트에서 쿠키 요청 허용
        // addExposedHeader("Authorization")는 클라이언트가 서버의 응답에서 특정 헤더를 읽을 수 있도록 노출하는 것
        configuration.addExposedHeader(HEADER_AUTHORIZATION); // 클라이언트가 서버의 응답에서 특정 헤더를 읽을 수 있도록 노출하는 것
        configuration.addExposedHeader(REFRESH_TOKEN_KEY); // 클라이언트가 서버의 응답에서 특정 헤더를 읽을 수 있도록 노출하는 것
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
