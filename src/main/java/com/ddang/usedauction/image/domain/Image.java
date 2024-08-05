package com.ddang.usedauction.image.domain;

import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.config.BaseTimeEntity;
import com.ddang.usedauction.image.dto.ImageServiceDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@SQLRestriction("deleted_at IS NULL")
public class Image extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageType imageType; // 이미지 타입

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Ask ask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @Column
    private LocalDateTime deletedAt;

    // entity -> serviceDto
    public ImageServiceDto toServiceDto() {

        return ImageServiceDto.builder()
            .id(id)
            .imageUrl(imageUrl)
            .imageType(imageType)
            .ask(ask.toServiceDto())
            .auction(auction.toServiceDto())
            .createdAt(getCreatedAt())
            .build();
    }
}
