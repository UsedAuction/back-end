package com.ddang.usedauction.category.dto;

import com.ddang.usedauction.category.domain.Category;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CategoryGetDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response {

        private Long id;
        private String categoryName;
        private Long parentId;
        private LocalDateTime createdAt;

        // entity -> getResponse
        public static CategoryGetDto.Response from(Category category) {

            return CategoryGetDto.Response.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .parentId(category.getParentId())
                .createdAt(category.getCreatedAt())
                .build();
        }
    }
}
