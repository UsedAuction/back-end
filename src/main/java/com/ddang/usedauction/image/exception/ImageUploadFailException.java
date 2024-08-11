package com.ddang.usedauction.image.exception;

import com.amazonaws.AmazonServiceException;

public class ImageUploadFailException extends AmazonServiceException {

    public ImageUploadFailException() {

        super("이미지 업로드에 실패하였습니다.");
    }
}
