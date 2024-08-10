package com.ddang.usedauction.image.dto;

import com.ddang.usedauction.image.domain.ImageType;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ImageGetDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response {

        private Long id;
        private String imageUrl;
        private String imageName;
        private ImageType imageType;
        private Long answerId;
        private Long auctionId;
        private LocalDateTime createdAt;
    }
}
