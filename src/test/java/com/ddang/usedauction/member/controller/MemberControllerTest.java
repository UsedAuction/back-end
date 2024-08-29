package com.ddang.usedauction.member.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.annotation.WithCustomMockUser;
import com.ddang.usedauction.config.SecurityConfig;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.domain.enums.Role;
import com.ddang.usedauction.member.dto.MemberChangeEmailDto;
import com.ddang.usedauction.member.dto.MemberChangePasswordDto;
import com.ddang.usedauction.member.dto.MemberCheckIdDto;
import com.ddang.usedauction.member.dto.MemberFindIdDto;
import com.ddang.usedauction.member.dto.MemberFindPasswordDto;
import com.ddang.usedauction.member.dto.MemberLoginRequestDto;
import com.ddang.usedauction.member.dto.MemberLoginResponseDto;
import com.ddang.usedauction.member.dto.MemberSignUpDto;
import com.ddang.usedauction.member.exception.MemberErrorCode;
import com.ddang.usedauction.member.exception.MemberException;
import com.ddang.usedauction.member.servie.MemberService;
import com.ddang.usedauction.security.auth.PrincipalOauth2UserService;
import com.ddang.usedauction.security.jwt.Oauth2FailureHandler;
import com.ddang.usedauction.security.jwt.Oauth2SuccessHandler;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.token.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({MemberController.class, SecurityConfig.class})
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TokenProvider tokenProvider;

    @MockBean
    RefreshTokenService refreshTokenService;

    @MockBean
    PrincipalOauth2UserService principalOauth2UserService;

    @MockBean
    Oauth2SuccessHandler oauth2SuccessHandler;

    @MockBean
    Oauth2FailureHandler oauth2FailureHandler;

    @MockBean
    MemberService memberService;

    @Test
    @WithCustomMockUser
    @DisplayName("회원 정보 조회 컨트롤러")
    void getMemberController() throws Exception {

        Member member = Member.builder()
            .memberId("memberId")
            .point(0)
            .email("test@naver.com")
            .role(Role.ROLE_USER)
            .passWord("1234")
            .siteAlarm(true)
            .build();

        when(memberService.getMember("memberId")).thenReturn(member);

        mockMvc.perform(get("/api/auth/members"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.memberId").value("memberId"))
            .andExpect(jsonPath("$.email").value("test@naver.com"));
    }

    @Test
    @DisplayName("회원 정보 조회 컨트롤러 실패 - 로그인 x")
    void getMemberControllerFail1() throws Exception {

        mockMvc.perform(get("/api/auth/members"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원 정보 조회 컨트롤러 실패 - url 경로 다름")
    void getMemberControllerFail2() throws Exception {

        mockMvc.perform(get("/api/auth/member"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login() throws Exception {
        MemberLoginRequestDto requestDto = MemberLoginRequestDto.builder()
            .memberId("test1234")
            .password("QWER12")
            .build();

        MemberLoginResponseDto responseDto = MemberLoginResponseDto.builder()
            .accessToken("accessToken")
            .memberId("test1234")
            .build();

        when(memberService.login(argThat(arg -> arg instanceof HttpServletResponse),
            argThat(arg -> arg.getMemberId().equals("test1234")))).thenReturn(
            responseDto);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("accessToken"))
            .andExpect(jsonPath("$.memberId").value("test1234"));
    }

    @Test
    @DisplayName("아이디 중복 확인 - 성공")
    void checkMemberId() throws Exception {
        MemberCheckIdDto dto = MemberCheckIdDto.builder()
            .memberId("test1234")
            .build();

        doNothing().when(memberService).checkMemberId(
            argThat(arg -> arg.getMemberId().equals(dto.getMemberId())));

        mockMvc.perform(get("/api/auth/check/id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string("사용 가능한 아이디 입니다."));
    }

    @Test
    @DisplayName("아이디 중복 확인 - 실패: 이미 존재하는 아이디")
    void checkMemberIdFail() throws Exception {
        MemberCheckIdDto dto = MemberCheckIdDto.builder()
            .memberId("test1234")
            .build();

        doThrow(new MemberException(MemberErrorCode.ALREADY_EXISTS_MEMBER_ID))
            .when(memberService).checkMemberId(
                argThat(arg -> arg.getMemberId().equals(dto.getMemberId())));

        mockMvc.perform(get("/api/auth/check/id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void signUp() throws Exception {
        MemberSignUpDto dto = MemberSignUpDto.builder()
            .memberId("test1234")
            .password("QWER12")
            .confirmPassword("QWER12")
            .email("test@email.com")
            .authNum("1234")
            .build();

        doNothing().when(memberService).signUp(dto);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(content().string("회원가입이 완료되었습니다."));
    }

    @Test
    @DisplayName("회원가입 - 유효성 검사 실패")
    void signUpFail() throws Exception {
        MemberSignUpDto dto = MemberSignUpDto.builder()
            .memberId("12")
            .password("qw2")
            .confirmPassword("qw2")
            .email("tel.com")
            .authNum("14")
            .build();

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("로그아웃 - 성공")
    @WithCustomMockUser
    void logout() throws Exception {

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원탈퇴 - 성공")
    @WithCustomMockUser
    void withdrawl() throws Exception {

        mockMvc.perform(post("/api/auth/withdrawl")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 변경 - 성공")
    @WithCustomMockUser
    void changeEmail() throws Exception {

        MemberChangeEmailDto dto = MemberChangeEmailDto.builder()
            .email("newEmail@email.com")
            .authNum("1234")
            .build();

        mockMvc.perform(patch("/api/auth/change/email")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 변경 - 실패: 유효성 검사 실패")
    @WithCustomMockUser
    void changeEmailFail() throws Exception {

        MemberChangeEmailDto dto = MemberChangeEmailDto.builder()
            .email("newEmail@.com")
            .authNum("134")
            .build();

        mockMvc.perform(patch("/api/auth/change/email")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    @WithCustomMockUser
    void changePassword() throws Exception {
        MemberChangePasswordDto dto = MemberChangePasswordDto.builder()
            .curPassword("currentPas!!s")
            .newPassword("newPass123!")
            .confirmPassword("newPass123!")
            .build();

        mockMvc.perform(patch("/api/auth/change/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호 변경 - 실패: 유효성 검사 실패")
    @WithCustomMockUser
    void changePasswordFail() throws Exception {

        MemberChangePasswordDto dto = MemberChangePasswordDto.builder()
            .curPassword("currentPass")
            .newPassword("newPass112312323!")
            .confirmPassword("newPass123!")
            .build();

        mockMvc.perform(patch("/api/auth/change/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원 아이디 찾기 - 성공")
    void findMemberId() throws Exception {
        MemberFindIdDto dto = MemberFindIdDto.builder()
            .email("test@email.com")
            .build();

        when(memberService.findMemberId(argThat(
            arg -> arg.getEmail().equals(dto.getEmail())
        ))).thenReturn("test1234");

        mockMvc.perform(post("/api/auth/find/id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원 아이디 찾기 - 실패: 유효성 검사 실패")
    void findMemberIdFail() throws Exception {
        MemberFindIdDto dto = MemberFindIdDto.builder()
            .email("tesail.com")
            .build();

        mockMvc.perform(post("/api/auth/find/id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 찾기 - 성공")
    void findPassword() throws Exception {
        MemberFindPasswordDto dto = MemberFindPasswordDto.builder()
            .memberId("test1234")
            .email("test@email.com")
            .build();

        mockMvc.perform(post("/api/auth/find/password")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호 찾기 - 실패: 유효성 검사 실패")
    void findPasswordFail() throws Exception {
        MemberFindPasswordDto dto = MemberFindPasswordDto.builder()
            .memberId("test")
            .email("test@emailcom")
            .build();

        mockMvc.perform(post("/api/auth/find/password")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}