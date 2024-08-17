package com.ddang.usedauction.member.controller;

import com.ddang.usedauction.member.dto.MemberGetDto.Response;
import jakarta.servlet.http.HttpSession;

public class HttpSessionUtils {
    public static final String MEMBER_SESSION_KEY = "sessionUser";

    private HttpSessionUtils() {
    }

    public static boolean isLoginMember(HttpSession session) {
        return getSessionMember(session) != null;
    }

    public static Response getSessionMember(HttpSession session) {
        return (Response) session.getAttribute(MEMBER_SESSION_KEY);
    }
}
