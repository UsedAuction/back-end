package com.ddang.usedauction.image.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageType {

    THUMBNAIL("thumbnail", "대표 이미지"),
    NORMAL("normal", "일반 이미지");

    private final String name;
    private final String description;
}
