package com.ddang.usedauction.answer.domain;

import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.config.BaseTimeEntity;
import com.ddang.usedauction.image.domain.Image;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
public class Answer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 제목

    @Column(nullable = false, length = 5000)
    private String content; // 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ask_id", nullable = false)
    private Ask ask; // 질문

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "answer", cascade = CascadeType.ALL)
    private List<Image> imageList; // 이미지 리스트

    @Column
    private LocalDateTime deletedAt;

    // 이미지 엔티티 함께 저장하는 메소드
    public void addImage(Image image) {

        imageList = imageList == null ? new ArrayList<>() : imageList;
        imageList.add(image);
    }
}
