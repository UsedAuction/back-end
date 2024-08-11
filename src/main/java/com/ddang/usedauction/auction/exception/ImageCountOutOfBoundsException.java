package com.ddang.usedauction.auction.exception;

public class ImageCountOutOfBoundsException extends IndexOutOfBoundsException {

    public ImageCountOutOfBoundsException(int cur) {
        super("최대 이미지 갯수는 6개 이지만 현재 이미지 갯수가" + cur + "개입니다.");
    }
}
