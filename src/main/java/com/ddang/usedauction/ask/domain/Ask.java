package com.ddang.usedauction.ask.domain;

import com.ddang.usedauction.ask.dto.AskServiceDto;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.config.BaseTimeEntity;
import com.ddang.usedauction.image.domain.Image;
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
public class Ask extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private Member writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @OneToMany(mappedBy = "ask", fetch = FetchType.LAZY)
    private List<Image> imageList;

    @Column
    private LocalDateTime deletedAt;

    // entity -> serviceDto
    public AskServiceDto toServiceDto() {

        return AskServiceDto.builder()
            .id(id)
            .title(title)
            .content(content)
            .writer(writer.toServiceDto())
            .auction(auction.toServiceDto())
            .imageList(imageList != null && !imageList.isEmpty() ? imageList.stream()
                .map(Image::toServiceDto).toList() : new ArrayList<>())
            .createdAt(getCreatedAt())
            .build();
    }
}
