package com.ddang.usedauction.image.dto;

import com.ddang.usedauction.answer.dto.AnswerServiceDto;
import com.ddang.usedauction.auction.dto.AuctionServiceDto;
import com.ddang.usedauction.image.domain.ImageType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class ImageServiceDto implements Serializable {

    private Long id;
    private String imageUrl;
    private ImageType imageType;
    private AnswerServiceDto answer;
    private AuctionServiceDto auction;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;
}
