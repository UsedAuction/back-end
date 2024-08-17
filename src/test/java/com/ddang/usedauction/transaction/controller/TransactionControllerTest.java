package com.ddang.usedauction.transaction.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.ReceiveType;
import com.ddang.usedauction.config.SecurityConfig;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.domain.ImageType;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.transaction.domain.BuyType;
import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.domain.Transaction;
import com.ddang.usedauction.transaction.service.TransactionService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({TransactionController.class, SecurityConfig.class})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    private List<Transaction> transactionList;

    @BeforeEach
    void setup() {

        Image image = Image.builder()
            .imageUrl("url")
            .imageType(ImageType.THUMBNAIL)
            .build();
        List<Image> imageList = List.of(image);

        Member seller = Member.builder()
            .memberId("seller")
            .email("seller@naver.com")
            .build();

        Auction auction1 = Auction.builder()
            .id(1L)
            .imageList(imageList)
            .productName("name1")
            .productStatus(3.5)
            .productColor("color")
            .startPrice(1000)
            .instantPrice(4000)
            .seller(seller)
            .receiveType(ReceiveType.ALL)
            .build();

        Auction auction2 = Auction.builder()
            .id(2L)
            .imageList(imageList)
            .productName("name2")
            .productStatus(3.5)
            .productColor("color")
            .startPrice(1000)
            .instantPrice(5000)
            .seller(seller)
            .receiveType(ReceiveType.CONTACT)
            .build();

        Member buyer = Member.builder()
            .memberId("buyer")
            .build();

        Transaction transaction1 = Transaction.builder()
            .buyer(buyer)
            .price(4000)
            .auction(auction1)
            .transType(TransType.SUCCESS)
            .buyType(BuyType.INSTANT)
            .build();

        Transaction transaction2 = Transaction.builder()
            .buyType(BuyType.INSTANT)
            .transType(TransType.SUCCESS)
            .auction(auction2)
            .price(5000)
            .buyer(buyer)
            .build();

        transactionList = List.of(transaction1, transaction2);
    }

    @Test
    @DisplayName("판매 내역 조회 컨트롤러")
    void getTransactionListBySellerController() throws Exception {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPageList = new PageImpl<>(transactionList, pageable,
            transactionList.size());

        when(
            transactionService.getTransactionListBySeller("test", "name", "end", null, null, null,
                pageable)).thenReturn(transactionPageList);

        mockMvc.perform(get("/api/transactions/sales?word=name&transTypeString=end"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].sellerEmail").value("seller@naver.com"))
            .andExpect(jsonPath("$.content[0].receiveType").value("ALL"));
    }

    @Test
    @DisplayName("판매 내역 조회 컨트롤러 실패 - url 경로 다름")
    void getTransactionListBySellerControllerFail1() throws Exception {

        mockMvc.perform(get("/api/transaction/sales?word=name&transTypeString=end"))
            .andDo(print())
            .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("구매 내역 조회 컨트롤러")
    void getTransactionListByBuyerController() throws Exception {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPageList = new PageImpl<>(transactionList, pageable,
            transactionList.size());

        when(
            transactionService.getTransactionListByBuyer("test", "name", "end", null, null, null,
                pageable)).thenReturn(transactionPageList);

        mockMvc.perform(get("/api/transactions/purchases?word=name&transTypeString=end"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].buyerId").value("buyer"))
            .andExpect(jsonPath("$.content[0].receiveType").value("ALL"));
    }

    @Test
    @DisplayName("구매 내역 조회 컨트롤러 실패 - url 경로 다름")
    void getTransactionListByBuyerControllerFail1() throws Exception {

        mockMvc.perform(get("/api/transaction/purchases?word=name&transTypeString=end"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}