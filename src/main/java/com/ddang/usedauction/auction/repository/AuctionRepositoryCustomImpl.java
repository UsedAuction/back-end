package com.ddang.usedauction.auction.repository;

import static com.ddang.usedauction.ask.domain.QAsk.ask;
import static com.ddang.usedauction.auction.domain.QAuction.auction;
import static com.ddang.usedauction.bid.domain.QBid.bid;
import static com.ddang.usedauction.image.domain.QImage.image;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.category.domain.QCategory;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class AuctionRepositoryCustomImpl implements AuctionRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Auction> findAllByOptions(String word, String mainCategory, String subCategory,
        String sorted,
        Pageable pageable) {

        List<Auction> auctionList = jpaQueryFactory.selectFrom(auction)
            .where(containsTitle(word), eqMainCategory(mainCategory), eqSubCategory(subCategory),
                auction.deletedAt.isNull(), auction.auctionState.eq(AuctionState.CONTINUE))
            .orderBy(getOrderSpecifier(sorted))
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch();

        long totalElements = jpaQueryFactory.selectFrom(auction)
            .where(containsTitle(word), eqMainCategory(mainCategory), eqSubCategory(subCategory),
                auction.deletedAt.isNull(), auction.auctionState.eq(AuctionState.CONTINUE))
            .fetch()
            .size();

        return new PageImpl<>(auctionList, pageable, totalElements);
    }

    @Override
    public List<Auction> findTop5(String mainCategory, String subCategory) {

        List<Auction> auctionList = jpaQueryFactory.selectFrom(auction)
            .leftJoin(auction.askList, ask)
            .innerJoin(auction.bidList, bid)
            .leftJoin(auction.imageList, image)
            .where(eqMainCategory(mainCategory), eqSubCategory(subCategory),
                auction.deletedAt.isNull())
            .limit(5)
            .fetch();

        return auctionList;
    }

    // 대분류 카테고리 일치 여부
    private BooleanExpression eqMainCategory(String mainCategory) {

        if (!StringUtils.hasText(mainCategory)) {
            return null;
        }

        QCategory parentCategory = auction.parentCategory;

        return parentCategory.categoryName.eq(mainCategory);
    }

    // 소분류 카테고리 일치 여부
    private BooleanExpression eqSubCategory(String subCategory) {

        if (!StringUtils.hasText(subCategory)) {
            return null;
        }

        QCategory childCategory = auction.childCategory;

        return childCategory.categoryName.eq(subCategory);
    }

    // 검색어와 경매글 제목의 포함 여부
    private BooleanExpression containsTitle(String title) {

        if (!StringUtils.hasText(title)) {
            return null;
        }

        return auction.title.containsIgnoreCase(title);
    }

    // 정렬 방법
    private OrderSpecifier<?> getOrderSpecifier(String sorted) {

        final String DATE = "date";
        final String LOW = "low";
        final String HIGH = "high";

        if (sorted == null) {
            sorted = "";
        }

        return switch (sorted) {
            case DATE -> auction.endedAt.asc();
            case LOW -> auction.currentPrice.asc();
            case HIGH -> auction.currentPrice.desc();
            default -> auction.createdAt.desc();
        };
    }
}
