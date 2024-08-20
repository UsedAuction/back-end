package com.ddang.usedauction.security.auth;

import com.ddang.usedauction.security.auth.userInfo.Oauth2UserInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * 사용자 인증 정보 관리
 */
public class PrincipalDetails implements UserDetails, OAuth2User {

  private String email;
  private String password;
  private String role;
  private Oauth2UserInfo oauth2UserInfo;

  public PrincipalDetails(String email, String password, String role) {
    this.email = email;
    this.password = password;
    this.role = role;
  }

  public PrincipalDetails(String email,
      String password, String role, Oauth2UserInfo oauth2UserInfo) {
    this.email = email;
    this.password = password;
    this.role = role;
    this.oauth2UserInfo = oauth2UserInfo;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> collection = new ArrayList<>();
    collection.add((GrantedAuthority) () -> role);
    return collection;
  }


  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return oauth2UserInfo.getAttributes();
  }

  @Override
  public String getName() {
    return email;
  }
}
