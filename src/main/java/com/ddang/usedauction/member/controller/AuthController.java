package com.ddang.usedauction.member.controller;

import com.ddang.usedauction.member.servie.AuthService;
import com.ddang.usedauction.token.dto.AccessTokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  /**
   * accessToken 재발급 컨트롤러
   *
   * @param accessToken 만료된 토큰
   * @return 성공 시 새로운 accessToken, 실패 시 에러코드와 에러메시지
   */
  @PostMapping("/reissue")
  public ResponseEntity<AccessTokenDto> reissueAccessToken(
      @RequestBody AccessTokenDto request) {
    String response = authService.reissueToken(request);
    return ResponseEntity.ok(AccessTokenDto.from(response));
  }

  /**
   * @param accessToken 사용자의 토큰
   */
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestBody AccessTokenDto request) {
    authService.deleteToken(request);
    return ResponseEntity.ok().build();
  }
}
