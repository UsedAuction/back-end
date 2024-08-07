package com.ddang.usedauction.category.exception;

import lombok.Getter;

@Getter
public class CategoryException extends RuntimeException {

    private final CategoryErrorCode categoryErrorCode;

    public CategoryException(CategoryErrorCode categoryErrorCode) {

        super(categoryErrorCode.getMessage());
        this.categoryErrorCode = categoryErrorCode;
    }
}
