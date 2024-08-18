package com.ddang.usedauction.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;

public class CookieUtil {

  public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      return Arrays.stream(cookies)
          .filter(cookie -> name.equals(cookie.getName()))
          .findFirst();
    }
    return Optional.empty();
  }

  public static Optional<String> getCookieValue(HttpServletRequest request, String name) {
    return getCookie(request, name).map(Cookie::getValue);
  }

  public static void addCookie(HttpServletResponse response, String name, String value,
      int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setMaxAge(maxAge);
    cookie.setPath("/");
    response.addCookie(cookie);
  }


  public static void deleteCookie(HttpServletRequest request, HttpServletResponse response,
      String name) {
    getCookie(request, name).ifPresent(cookie -> {
      cookie.setValue("");
      cookie.setPath("/");
      cookie.setMaxAge(0);
      response.addCookie(cookie);
    });
  }
}
