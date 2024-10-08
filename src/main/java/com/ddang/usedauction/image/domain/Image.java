package com.ddang.usedauction.image.domain;

import com.ddang.usedauction.answer.domain.Answer;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.config.BaseTimeEntity;
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
    private String imageName; // s3에 저장된 이미지 이름

    @Column(nullable = false, length = 5000)
    private String imageUrl; // s3에 저장된 이미지 url

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageType imageType; // 이미지 타입

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id")
    private Answer answer; // 답변글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @Column
    private LocalDateTime deletedAt;
}
