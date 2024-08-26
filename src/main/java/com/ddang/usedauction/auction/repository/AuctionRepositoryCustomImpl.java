package com.ddang.usedauction.auction.repository;

import static com.ddang.usedauction.ask.domain.QAsk.ask;
import static com.ddang.usedauction.auction.domain.QAuction.auction;
import static com.ddang.usedauction.bid.domain.QBid.bid;
import static com.ddang.usedauction.image.domain.QImage.image;

import com.ddang.usedauction.auction.domain.Auction;
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
            .leftJoin(auction.askList, ask)
            .leftJoin(auction.bidList, bid)
            .leftJoin(auction.imageList, image)
            .where(containsTitle(word), eqMainCategory(mainCategory), eqSubCategory(subCategory),
                auction.deletedAt.isNull())
            .orderBy(getOrderSpecifier(sorted))
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch();

        long totalElements = jpaQueryFactory.selectFrom(auction)
            .where(containsTitle(word), eqMainCategory(mainCategory), auction.deletedAt.isNull())
            .fetch()
            .size();

        return new PageImpl<>(auctionList, pageable, totalElements);
    }

    // 검색어와 경매글 제목의 포함 여부
    private BooleanExpression containsTitle(String title) {

        if (!StringUtils.hasText(title)) {
            return null;
        }

        return auction.title.containsIgnoreCase(title);
    }

    // 대분류 카테고리 일치 여부
    private BooleanExpression eqMainCategory(String mainCategoryName) {

        if (!StringUtils.hasText(mainCategoryName)) {
            return null;
        }

        QCategory parentCategory = auction.parentCategory;

        return parentCategory.categoryName.eq(mainCategoryName);
    }

    private BooleanExpression eqSubCategory(String subCategoryName) {

        if (!StringUtils.hasText(subCategoryName)) {
            return null;
        }

        QCategory childCategory = auction.childCategory;

        return childCategory.categoryName.eq(subCategoryName);
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
