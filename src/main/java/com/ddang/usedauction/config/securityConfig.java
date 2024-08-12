package com.ddang.usedauction.config;

import com.ddang.usedauction.global.security.auth.PrincipalOauth2UserService;
import com.ddang.usedauction.global.security.jwt.JwtAuthenticationFilter;
import com.ddang.usedauction.global.security.jwt.Oauth2FailureHandler;
import com.ddang.usedauction.global.security.jwt.Oauth2SuccessHandler;
import com.ddang.usedauction.global.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@RequiredArgsConstructor
public class securityConfig {

  private final TokenProvider tokenProvider;
  private final PrincipalOauth2UserService principalOauth2UserService;
  private final Oauth2SuccessHandler oauth2SuccessHandler;
  private final Oauth2FailureHandler oauth2FailureHandler;

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
            .anyRequest().permitAll()
        )
        .oauth2Login(oauth -> // OAuth2 로그인 기능에 대한 여러 설정의 진입점
            // OAuth2 로그인 성공 이후 사용자 정보를 가져올 때의 설정을 담당
            oauth.userInfoEndpoint(userInfo -> userInfo.userService(principalOauth2UserService))
                // 로그인 성공 시 핸들러
                .successHandler(oauth2SuccessHandler)
                .failureHandler(oauth2FailureHandler)
        )
        .addFilterBefore(new JwtAuthenticationFilter(tokenProvider),
            UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
