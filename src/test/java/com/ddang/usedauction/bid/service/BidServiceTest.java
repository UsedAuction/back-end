package com.ddang.usedauction.bid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.bid.dto.BidGetDto;
import com.ddang.usedauction.bid.repository.BidRepository;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.domain.ImageType;
import com.ddang.usedauction.member.domain.Member;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private BidService bidService;

    @Test
    @DisplayName("회원의 입찰 목록 조회")
    void getBidList() {

        Image image = Image.builder()
            .imageType(ImageType.THUMBNAIL)
            .imageUrl("url")
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .title("title")
            .imageList(List.of(image))
            .build();

        Member member = Member.builder()
            .memberId("test")
            .build();

        Bid bid = Bid.builder()
            .bidPrice(5000)
            .auction(auction)
            .member(member)
            .build();
        List<Bid> bidList = List.of(bid);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Bid> bidPageList = new PageImpl<>(bidList, pageable, bidList.size());

        when(bidRepository.findAllByMemberId("test", pageable)).thenReturn(bidPageList);

        Page<BidGetDto.Response> result = bidService.getBidList("test", pageable);

        assertEquals(5000, result.getContent().get(0).getBidPrice());
    }
}