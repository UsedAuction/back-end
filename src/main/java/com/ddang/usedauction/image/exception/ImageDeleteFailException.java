package com.ddang.usedauction.image.exception;

import com.amazonaws.AmazonServiceException;

public class ImageDeleteFailException extends AmazonServiceException {

    public ImageDeleteFailException() {

        super("이미지 삭제에 실패하였습니다.");
    }
}
