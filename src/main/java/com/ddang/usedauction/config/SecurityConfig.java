package com.ddang.usedauction.config;

import com.ddang.usedauction.security.auth.PrincipalOauth2UserService;
import com.ddang.usedauction.security.jwt.JwtAccessDeniedHandler;
import com.ddang.usedauction.security.jwt.JwtAuthenticationEntryPoint;
import com.ddang.usedauction.security.jwt.JwtAuthenticationFilter;
import com.ddang.usedauction.security.jwt.Oauth2FailureHandler;
import com.ddang.usedauction.security.jwt.Oauth2SuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final PrincipalOauth2UserService principalOauth2UserService;
    private final Oauth2SuccessHandler oauth2SuccessHandler;
    private final Oauth2FailureHandler oauth2FailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .httpBasic(HttpBasicConfigurer::disable)
            .csrf(CsrfConfigurer::disable)
            .sessionManagement(sessionManagement
                -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(
                headersConfigurer -> headersConfigurer.frameOptions(
                    HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auctions").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/auctions/{auctionId}/confirm")
                .authenticated()
                .requestMatchers(HttpMethod.POST, "/api/auctions/{auctionId}").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/bids").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/transactions/sales").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/transactions/purchases").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/members/orders/create").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/members/points").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/members/points/history").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/members/payment/ready").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/members/payment/approve").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/members/notification/subscribe").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/members/notification").authenticated()
                .anyRequest().permitAll()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint(objectMapper))
                .accessDeniedHandler(new JwtAccessDeniedHandler(objectMapper)))
            .oauth2Login(oauth -> oauth// OAuth2 로그인 기능에 대한 여러 설정의 진입점
                // 로그인 성공 시 핸들러
                .successHandler(oauth2SuccessHandler)
                // 로그인 실패 시 핸들러
                .failureHandler(oauth2FailureHandler)
                // OAuth2 로그인 성공 이후 사용자 정보를 가져올 때의 설정을 담당
                .userInfoEndpoint(userInfo -> userInfo.userService(principalOauth2UserService))
            )

            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
