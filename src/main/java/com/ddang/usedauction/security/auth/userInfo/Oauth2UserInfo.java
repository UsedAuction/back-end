package com.ddang.usedauction.security.auth.userInfo;

import java.util.Map;

public interface Oauth2UserInfo {

  Map<String, Object> getAttributes();

  String getProviderId();

  String getProvider();

  String getEmail();

  String getName();
}
