package com.ddang.usedauction.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.http.ResponseCookie;

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
        ResponseCookie cookie = ResponseCookie.from(name, value)
            .domain("https://dddang.vercel.app")
            .maxAge(maxAge)
            .path("/")
            .httpOnly(true)
            .sameSite("NONE")
            .secure(true)
            .build();

        response.setHeader("Set-Cookie", cookie.toString());
    }


    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response,
        String name) {

        Cookie cookie = getCookie(request, name)
            .orElseThrow(() -> new NoSuchElementException("쿠키가 존재하지 않습니다."));

        cookie.setValue("");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setDomain("https://dddang.vercel.app");
        response.addCookie(cookie);
    }
}
