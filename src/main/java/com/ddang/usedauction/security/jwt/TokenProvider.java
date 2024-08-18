package com.ddang.usedauction.security.jwt;

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
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TokenProvider {

  @Value("${spring.datasource.jwt.secret}")
  private String secretKey;
  @Value("${spring.datasource.jwt.access.expiration}")
  private Long accessExpiration;
  @Value("${spring.datasource.jwt.refresh.expiration}")
  private Long refreshExpiration;
  private final RefreshTokenService refreshTokenService;
  private Key key;

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
  }

  public TokenDto generateToken(String email, Collection<? extends GrantedAuthority> authorities) {
    long now = new Date().getTime();

    String accessToken = Jwts.builder()
        .setSubject(email)
        .claim("auth", authorities)
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
        .claim("auth", authorities)
        .setExpiration(new Date(now + accessExpiration))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Authentication getAuthentication(String token) {
    Claims claims = parseClaims(token);

    if (claims.get("auth") == null) {
      throw new CustomJwtException(JwtErrorCode.INVALID_TOKEN);
    }

    Collection<? extends GrantedAuthority> authorities =
        Arrays.stream(claims.get("auth").toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    UserDetails principal = new User(claims.getSubject(), "", authorities);
    return new UsernamePasswordAuthenticationToken(principal, "", authorities);
  }

  private Claims parseClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (ExpiredJwtException e) {
      throw new JwtException("만료되거나 유효하지 않은 토큰입니다.");
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
    } catch (IllegalArgumentException e) {
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
