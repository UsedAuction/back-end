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

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public TokenDto generateToken(String email,
        Collection<? extends GrantedAuthority> authorities) {
        long now = new Date().getTime();

        String accessToken = Jwts.builder()
            .setSubject(email)
            .claim("auth", List.of("ROLE_USER"))
            .setExpiration(new Date(now + accessExpiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        String refreshToken = Jwts.builder()
            .setSubject(email)
            .setExpiration(new Date(now + refreshExpiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        return TokenDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    public String reissueAccessToken(String email,
        Collection<? extends GrantedAuthority> authorities) {
        long now = new Date().getTime();

        return Jwts.builder()
            .setSubject(email)
            .claim("auth", List.of("ROLE_USER"))
            .setExpiration(new Date(now + accessExpiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    @Transactional
    public Authentication getAuthentication(String token) {

        PrincipalDetails userDetails = (PrincipalDetails) principalDetailsService.loadUserByUsername(
            getEmailByToken(token));

        log.info("userDetails = {} {}", userDetails.getAuthorities(), userDetails.getName());
        log.info("role = {}",
            new UsernamePasswordAuthenticationToken(userDetails, "",
                userDetails.getAuthorities()).getAuthorities());

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

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            if (refreshTokenService.hasKeyBlackList(token)) {
                return false;
            }
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            throw new CustomJwtException(JwtErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new CustomJwtException(JwtErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new CustomJwtException(JwtErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException | SignatureException e) {
            throw new CustomJwtException(JwtErrorCode.INVALID_TOKEN);
        }
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

    public String getEmailByToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    public Long getExpiration(String accessToken) {
        Claims claims = parseClaims(accessToken);
        Date expiration = claims.getExpiration();

        return expiration.getTime() - new Date().getTime();
    }
}
