package com.ddang.usedauction.category.dto;

import com.ddang.usedauction.category.domain.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class CategoryDto {

    private Long id;
    private String categoryName;
    private String imageUrl;
    private List<ChildCategoryDto> categories;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
    private LocalDateTime createdAt;

    public static CategoryDto from(Category category, List<ChildCategoryDto> categories) {

        return CategoryDto.builder()
            .id(category.getId())
            .categoryName(category.getCategoryName())
            .imageUrl(category.getImageUrl())
            .createdAt(category.getCreatedAt())
            .categories(categories)
            .build();
    }
}
