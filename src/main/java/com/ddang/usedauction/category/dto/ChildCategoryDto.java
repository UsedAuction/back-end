package com.ddang.usedauction.category.dto;

import com.ddang.usedauction.category.domain.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
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
public class ChildCategoryDto {

    private long id;
    private String categoryName;
    private Long parentId;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
    private LocalDateTime createdAt;

    public static ChildCategoryDto from(Category category) {

        return ChildCategoryDto.builder()
            .id(category.getId())
            .categoryName(category.getCategoryName())
            .parentId(category.getParentId())
            .createdAt(category.getCreatedAt())
            .build();
    }
}
