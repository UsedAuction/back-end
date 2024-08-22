package com.ddang.usedauction.bid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.bid.repository.BidRepository;
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

        Bid bid = Bid.builder()
            .bidPrice(5000)
            .build();
        List<Bid> bidList = List.of(bid);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Bid> bidPageList = new PageImpl<>(bidList, pageable, bidList.size());

        when(bidRepository.findAllByMemberEmail("test", pageable)).thenReturn(bidPageList);

        Page<Bid> result = bidService.getBidList("test", pageable);

        assertEquals(5000, result.getContent().get(0).getBidPrice());
    }
}