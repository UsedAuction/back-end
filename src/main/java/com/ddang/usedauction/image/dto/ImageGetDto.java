package com.ddang.usedauction.image.dto;

import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.domain.ImageType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
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

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
        private LocalDateTime createdAt;

        // entity -> getResponse
        public static ImageGetDto.Response from(Image image) {

            return Response.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .imageName(image.getImageName())
                .imageType(image.getImageType())
                .answerId(image.getAnswer() != null ? image.getAnswer().getId() : null)
                .auctionId(image.getAuction() != null ? image.getAuction().getId() : null)
                .createdAt(image.getCreatedAt())
                .build();
        }
    }
}
