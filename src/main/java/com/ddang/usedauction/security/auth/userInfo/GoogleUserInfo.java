package com.ddang.usedauction.security.auth.userInfo;

import java.util.Map;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GoogleUserInfo implements Oauth2UserInfo {

  private Map<String, Object> attributes;

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getProviderId() {
    return attributes.get("sub").toString();
  }

  @Override
  public String getProvider() {
    return "Google";
  }

  @Override
  public String getEmail() {
    return attributes.get("email").toString();
  }

  @Override
  public String getName() {
    return attributes.get("name").toString();
  }
}
