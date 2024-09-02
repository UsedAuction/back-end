package com.ddang.usedauction.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.ddang.usedauction.auction.domain.ReceiveType;
import com.ddang.usedauction.category.domain.Category;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.domain.ImageType;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.domain.Transaction;
import com.ddang.usedauction.transaction.dto.TransactionDto;
import com.ddang.usedauction.transaction.dto.TransactionGetDto;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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
    @DisplayName("경매 pk로 거래 조회")
    void getTransaction() {

        Member seller = Member.builder()
            .memberId("seller")
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .seller(seller)
            .build();

        Transaction transaction = Transaction.builder()
            .id(1L)
            .price(5000)
            .auction(auction)
            .build();

        when(transactionRepository.findByAuctionId(1L)).thenReturn(Optional.of(transaction));

        TransactionDto result = transactionService.getTransaction(1L);

        assertEquals(5000, result.getPrice());
    }

    @Test
    @DisplayName("경매 pk로 거래 조회 실패 - 없는 거래")
    void getTransactionFail1() {

        when(transactionRepository.findByAuctionId(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> transactionService.getTransaction(1L));
    }

    @Test
    @DisplayName("판매 내역 조회")
    void getTransactionListBySeller() {

        Member seller = Member.builder()
            .memberId("seller")
            .build();

        Image image = Image.builder()
            .imageName("name")
            .imageType(ImageType.THUMBNAIL)
            .imageUrl("url")
            .id(1L)
            .build();

        Category category2 = Category.builder()
            .categoryName("category2")
            .build();

        Category childCategory = Category.builder()
            .categoryName("child")
            .build();

        Auction auction1 = Auction.builder()
            .seller(seller)
            .productName("name1")
            .receiveType(ReceiveType.ALL)
            .id(1L)
            .title("title")
            .imageList(List.of(image))
            .parentCategory(category2)
            .currentPrice(2000)
            .endedAt(LocalDateTime.now().plusDays(1))
            .auctionState(AuctionState.CONTINUE)
            .instantPrice(4000)
            .startPrice(2000)
            .productStatus(3.5)
            .productColor("color")
            .childCategory(childCategory)
            .deliveryType(DeliveryType.NO_PREPAY)
            .contactPlace("place")
            .productDescription("description")
            .build();

        Auction auction2 = Auction.builder()
            .seller(seller)
            .productName("name2")
            .receiveType(ReceiveType.ALL)
            .id(1L)
            .title("title")
            .imageList(List.of(image))
            .parentCategory(category2)
            .currentPrice(2000)
            .endedAt(LocalDateTime.now().plusDays(1))
            .auctionState(AuctionState.CONTINUE)
            .instantPrice(4000)
            .startPrice(2000)
            .productStatus(3.5)
            .productColor("color")
            .childCategory(childCategory)
            .deliveryType(DeliveryType.NO_PREPAY)
            .contactPlace("place")
            .productDescription("description")
            .build();

        Transaction transaction1 = Transaction.builder()
            .id(1L)
            .transType(TransType.CONTINUE)
            .price(2000)
            .auction(auction1)
            .build();

        Transaction transaction2 = Transaction.builder()
            .id(2L)
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

        Page<TransactionGetDto.Response> resultList = transactionService.getTransactionListBySeller(
            "seller", null,
            null, null, null, null, pageable);

        assertEquals(2, resultList.getTotalElements());
    }

    @Test
    @DisplayName("구매 내역 조회")
    void getTransactionListByBuyer() {

        Member buyer = Member.builder()
            .memberId("buyer")
            .build();

        Member seller = Member.builder()
            .memberId("seller")
            .build();

        Image image = Image.builder()
            .imageName("name")
            .imageType(ImageType.THUMBNAIL)
            .imageUrl("url")
            .id(1L)
            .build();

        Category category2 = Category.builder()
            .categoryName("category2")
            .build();

        Category childCategory = Category.builder()
            .categoryName("child")
            .build();

        Auction auction1 = Auction.builder()
            .productName("name1")
            .seller(seller)
            .receiveType(ReceiveType.ALL)
            .id(1L)
            .title("title")
            .imageList(List.of(image))
            .parentCategory(category2)
            .currentPrice(2000)
            .endedAt(LocalDateTime.now().plusDays(1))
            .auctionState(AuctionState.CONTINUE)
            .instantPrice(4000)
            .startPrice(2000)
            .productStatus(3.5)
            .productColor("color")
            .childCategory(childCategory)
            .deliveryType(DeliveryType.NO_PREPAY)
            .contactPlace("place")
            .productDescription("description")
            .build();

        Auction auction2 = Auction.builder()
            .productName("name2")
            .seller(seller)
            .receiveType(ReceiveType.ALL)
            .id(1L)
            .title("title")
            .imageList(List.of(image))
            .parentCategory(category2)
            .currentPrice(2000)
            .endedAt(LocalDateTime.now().plusDays(1))
            .auctionState(AuctionState.CONTINUE)
            .instantPrice(4000)
            .startPrice(2000)
            .productStatus(3.5)
            .productColor("color")
            .childCategory(childCategory)
            .deliveryType(DeliveryType.NO_PREPAY)
            .contactPlace("place")
            .productDescription("description")
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

        Page<TransactionGetDto.Response> resultList = transactionService.getTransactionListByBuyer(
            "buyer", null,
            null,
            null, null, null, pageable);

        assertEquals(2, resultList.getTotalElements());
    }
}