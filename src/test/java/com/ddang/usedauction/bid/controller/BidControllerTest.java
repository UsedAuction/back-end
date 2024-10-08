package com.ddang.usedauction.bid.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.annotation.WithCustomMockUser;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.bid.dto.BidGetDto;
import com.ddang.usedauction.bid.service.BidService;
import com.ddang.usedauction.config.SecurityConfig;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.domain.ImageType;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.security.auth.PrincipalOauth2UserService;
import com.ddang.usedauction.security.jwt.Oauth2FailureHandler;
import com.ddang.usedauction.security.jwt.Oauth2SuccessHandler;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.token.service.RefreshTokenService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({BidController.class, SecurityConfig.class})
class BidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrincipalOauth2UserService principalOauth2UserService;

    @MockBean
    private Oauth2SuccessHandler oauth2SuccessHandler;

    @MockBean
    private Oauth2FailureHandler oauth2FailureHandler;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private BidService bidService;

    @Test
    @WithCustomMockUser
    @DisplayName("회원의 입찰 목록 조회 컨트롤러")
    void getBidListController() throws Exception {

        Image image = Image.builder()
            .imageType(ImageType.THUMBNAIL)
            .imageUrl("url")
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .imageList(List.of(image))
            .build();

        Member member = Member.builder()
            .memberId("memberId")
            .build();

        Bid bid = Bid.builder()
            .id(1L)
            .bidPrice(1000)
            .auction(auction)
            .member(member)
            .build();
        List<BidGetDto.Response> bidList = List.of(BidGetDto.Response.from(bid));
        Pageable pageable = PageRequest.of(0, 10, Direction.DESC, "createdAt");
        Page<BidGetDto.Response> bidPageList = new PageImpl<>(bidList, pageable, bidList.size());

        when(bidService.getBidList("memberId", pageable)).thenReturn(bidPageList);

        mockMvc.perform(get("/api/bids"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].bidPrice").value(1000));
    }

    @Test
    @DisplayName("회원의 입찰 목록 조회 컨트롤러 실패 - url 경로 다름")
    void getBidListControllerFail1() throws Exception {

        mockMvc.perform(get("/api/bid"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}