package com.ddang.usedauction.member.controller;

import com.ddang.usedauction.member.domain.Member;
import jakarta.servlet.http.HttpSession;

public class HttpSessionUtils {
    public static final String MEMBER_SESSION_KEY = "sessionUser";

    private HttpSessionUtils() {
    }

    public static boolean isLoginMember(HttpSession session) {
        return getSessionMember(session) != null;
    }

    public static Member getSessionMember(HttpSession session) {
        return (Member) session.getAttribute(MEMBER_SESSION_KEY);
    }
}

