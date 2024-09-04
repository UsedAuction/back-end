package com.ddang.usedauction.security.jwt;

import com.ddang.usedauction.security.auth.PrincipalDetails;
import com.ddang.usedauction.security.auth.PrincipalDetailsService;
import com.ddang.usedauction.security.jwt.exception.CustomJwtException;
import com.ddang.usedauction.security.jwt.exception.JwtErrorCode;
import com.ddang.usedauction.token.dto.TokenDto;
import com.ddang.usedauction.token.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenProvider {

    @Value("${spring.jwt.secret}")
    private String secretKey;
    @Value("${spring.jwt.access.expiration}")
    private Long accessExpiration;
    @Value("${spring.jwt.refresh.expiration}")
    private Long refreshExpiration;
    private final RefreshTokenService refreshTokenService;
    private final PrincipalDetailsService principalDetailsService;
    private Key key;

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public TokenDto generateToken(String memberId,
        Collection<? extends GrantedAuthority> authorities) {
        long now = new Date().getTime();

        String accessToken = Jwts.builder()
            .setSubject(memberId)
            .claim("auth", List.of("ROLE_USER"))
            .setExpiration(new Date(now + 300000))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        String refreshToken = Jwts.builder()
            .setSubject(memberId)
            .setExpiration(new Date(now + refreshExpiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        return TokenDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    public String reissueAccessToken(String memberId,
        Collection<? extends GrantedAuthority> authorities) {
        long now = new Date().getTime();

        return Jwts.builder()
            .setSubject(memberId)
            .claim("auth", List.of("ROLE_USER"))
            .setExpiration(new Date(now + 300000))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    // 헤더에서 토큰 가져오기
    public String resolveTokenFromRequest(HttpServletRequest request) {

        String token = request.getHeader(TOKEN_HEADER);

        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());
        }

        return null;
    }

    @Transactional
    public Authentication getAuthentication(String token) {

        PrincipalDetails userDetails = (PrincipalDetails) principalDetailsService.loadUserByUsername(
            getMemberIdByToken(token));

        return new UsernamePasswordAuthenticationToken(userDetails, "",
            userDetails.getAuthorities());
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (JwtException e) {
            throw new CustomJwtException(JwtErrorCode.INVALID_TOKEN);
        }
    }

    public boolean validateToken(String accessToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

            return !refreshTokenService.hasKeyBlackList(accessToken) && !claims.getExpiration()
                .before(new Date());
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰입니다.");
        } catch (SecurityException | MalformedJwtException e) {
            log.error("유효하지 않은 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("토큰이 비어있습니다.");
        } catch (SignatureException e) {
            log.error("잘못된 서명의 토큰입니다.");
        }

        return false;
    }

    public boolean isExpiredToken(String token) {
        try {
            return parseClaims(token)
                .getExpiration()
                .before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getMemberIdByToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    public Long getExpiration(String token) {
        Claims claims = parseClaims(token);
        Date expiration = claims.getExpiration();

        return expiration.getTime() - new Date().getTime();
    }

}
