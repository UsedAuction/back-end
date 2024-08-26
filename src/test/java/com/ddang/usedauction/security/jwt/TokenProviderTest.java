package com.ddang.usedauction.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.security.auth.PrincipalDetails;
import com.ddang.usedauction.security.auth.PrincipalDetailsService;
import com.ddang.usedauction.token.dto.TokenDto;
import com.ddang.usedauction.token.service.RefreshTokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TokenProviderTest {

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    PrincipalDetailsService principalDetailsService;

    @InjectMocks
    TokenProvider tokenProvider;

    SecretKey key;

    String secretKey = "234sdfjiwoefjwjklwejrwejrsdfjklwasdsaddwqdqdqwf";
    final Long accessTokenExpiration = 3600000L;
    final Long refreshTokenExpiration = 86400000L;

    @BeforeEach
    void setUp() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        ReflectionTestUtils.setField(tokenProvider, "secretKey", secretKey);
        ReflectionTestUtils.setField(tokenProvider, "refreshExpiration", refreshTokenExpiration);
        ReflectionTestUtils.setField(tokenProvider, "accessExpiration", accessTokenExpiration);
        ReflectionTestUtils.setField(tokenProvider, "key", key);
        tokenProvider.init();
    }


    @Test
    @DisplayName("토큰 생성 - 성공")
    void generateToken() {
        //given
        String email = "test@email.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("USER"));
        //when
        TokenDto token = tokenProvider.generateToken(email, authorities);
        //then
        assertNotNull(token);
        assertNotNull(token.getAccessToken());
        assertNotNull(token.getRefreshToken());
    }

    @Test
    @DisplayName("토큰으로 인증 정보 가져오기 - 성공")
    void getAuthentication() {
        //given
        String email = "test@email.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"));
        String token = tokenProvider.generateToken(email, authorities).getAccessToken();
        PrincipalDetails userDetails = new PrincipalDetails("test@email.com", "1234", "ROLE_USER");

        when(principalDetailsService.loadUserByUsername("test@email.com")).thenReturn(userDetails);
        //when
        Authentication authentication = tokenProvider.getAuthentication(token);
        //then
        assertNotNull(token);
        assertEquals(email, authentication.getName());
    }


    @Test
    @DisplayName("유효성 검사 - 성공")
    void validateToken() {
        long now = (new Date()).getTime();
        Date expirationDate = new Date(now + accessTokenExpiration);

        String token = Jwts.builder()
            .setSubject("test@email.com")
            .setExpiration(expirationDate)
            .signWith(key)
            .compact();

        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("유효성 검사 - 만료된 토큰으로 인한 실패")
    void validateExpiredToken() {
        long now = (new Date()).getTime();
        Date expirationDate = new Date(now - 1000);

        String token = Jwts.builder()
            .setSubject("test@email.com")
            .setExpiration(expirationDate)
            .signWith(key)
            .compact();

        boolean result = tokenProvider.validateToken(token);

        assertFalse(result);
    }
    
    @Test
    @DisplayName("블랙리스트에 있는 토큰인지 테스트")
    void validateBlacklistedToken() {
        long now = (new Date()).getTime();
        Date expirationDate = new Date(now + accessTokenExpiration);

        String blacklistedToken = Jwts.builder()
            .setSubject("test@email.com")
            .setExpiration(expirationDate)
            .signWith(key)
            .compact();

        when(refreshTokenService.hasKeyBlackList(blacklistedToken)).thenReturn(true);

        // when / then
        assertFalse(tokenProvider.validateToken(blacklistedToken));
    }

    @Test
    @DisplayName("토큰 만료 시간 가져오기")
    void getExpiration() {
        long now = (new Date()).getTime();
        String token = Jwts.builder()
            .setSubject("test@email.com")
            .setExpiration(new Date(now + accessTokenExpiration))
            .signWith(key)
            .compact();

        Long expiration = tokenProvider.getExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration > 0);
    }

    @Test
    @DisplayName("토큰 만료 여부 확인 - 만료된 토큰")
    void isExpiredToken() {
        long now = (new Date()).getTime();
        Date expirationDate = new Date(now - accessTokenExpiration);

        String expiredToken = Jwts.builder()
            .setSubject("test@email.com")
            .setExpiration(expirationDate)
            .signWith(key)
            .compact();

        assertTrue(tokenProvider.isExpiredToken(expiredToken));
    }

    @Test
    @DisplayName("토큰으로 유저 이메일 추출")
    void getEmailByToken() {
        String email = "test@email.com";
        long now = (new Date()).getTime();
        Date expirationDate = new Date(now + accessTokenExpiration);

        String token = Jwts.builder()
            .setSubject(email)
            .setExpiration(expirationDate)
            .signWith(key)
            .compact();

        String extractedEmail = tokenProvider.getEmailByToken(token);

        assertEquals(email, extractedEmail);
    }
}