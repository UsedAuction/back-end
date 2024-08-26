package com.ddang.usedauction.ask.domain;

import com.ddang.usedauction.answer.domain.Answer;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.config.BaseTimeEntity;
import com.ddang.usedauction.member.domain.Member;
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
public class Ask extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 제목

    @Column(nullable = false, length = 5000)
    private String content; // 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private Member writer; // 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction; // 경매

    @OneToMany(mappedBy = "ask", fetch = FetchType.LAZY)
    private List<Answer> answerList; // 답변

    @Column
    private LocalDateTime deletedAt;
}
