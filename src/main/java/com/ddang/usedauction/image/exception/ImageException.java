package com.ddang.usedauction.image.exception;

import lombok.Getter;

@Getter
public class ImageException extends RuntimeException {

    private final ImageErrorCode imageErrorCode;

    public ImageException(ImageErrorCode imageErrorCode) {

        super(imageErrorCode.getMessage());
        this.imageErrorCode = imageErrorCode;
    }
}
