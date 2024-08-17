package com.ddang.usedauction.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.ReceiveType;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.domain.Transaction;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
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
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("판매 내역 조회")
    void getTransactionListBySeller() {

        Member seller = Member.builder()
            .memberId("seller")
            .build();

        Auction auction1 = Auction.builder()
            .seller(seller)
            .productName("name1")
            .receiveType(ReceiveType.ALL)
            .build();

        Auction auction2 = Auction.builder()
            .seller(seller)
            .productName("name2")
            .build();

        Transaction transaction1 = Transaction.builder()
            .transType(TransType.CONTINUE)
            .price(2000)
            .auction(auction1)
            .build();

        Transaction transaction2 = Transaction.builder()
            .transType(TransType.SUCCESS)
            .price(3000)
            .auction(auction2)
            .build();

        List<Transaction> transactionList = List.of(transaction1, transaction2);

        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<Transaction> transactionPageList = new PageImpl<>(transactionList, pageable,
            transactionList.size());

        when(
            transactionRepository.findAllByTransactionListBySeller("seller", null, null, null, null,
                null, pageable)).thenReturn(transactionPageList);

        Page<Transaction> resultList = transactionService.getTransactionListBySeller("seller", null,
            null, null, null, null, pageable);

        assertEquals(2, resultList.getTotalElements());
        assertEquals(2000, resultList.getContent().get(0).getPrice());
        assertEquals("seller",
            resultList.getContent().get(0).getAuction().getSeller().getMemberId());
        assertEquals(ReceiveType.ALL, resultList.getContent().get(0).getAuction().getReceiveType());
    }

    @Test
    @DisplayName("구매 내역 조회")
    void getTransactionListByBuyer() {

        Member buyer = Member.builder()
            .memberId("buyer")
            .build();

        Auction auction1 = Auction.builder()
            .productName("name1")
            .build();

        Auction auction2 = Auction.builder()
            .productName("name2")
            .build();

        Transaction transaction1 = Transaction.builder()
            .transType(TransType.CONTINUE)
            .price(2000)
            .auction(auction1)
            .buyer(buyer)
            .build();

        Transaction transaction2 = Transaction.builder()
            .transType(TransType.SUCCESS)
            .price(3000)
            .auction(auction2)
            .buyer(buyer)
            .build();

        List<Transaction> transactionList = List.of(transaction1, transaction2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPageList = new PageImpl<>(transactionList, pageable,
            transactionList.size());

        when(transactionRepository.findAllByTransactionListByBuyer("buyer", null, null, null, null,
            null, pageable)).thenReturn(transactionPageList);

        Page<Transaction> resultList = transactionService.getTransactionListByBuyer("buyer", null,
            null,
            null, null, null, pageable);

        assertEquals(2, resultList.getTotalElements());
        assertEquals("buyer", resultList.getContent().get(0).getBuyer().getMemberId());
    }
}