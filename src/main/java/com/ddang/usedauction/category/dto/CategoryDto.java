package com.ddang.usedauction.category.dto;

import com.ddang.usedauction.category.domain.Category;
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

    public static CategoryDto from(Category category, List<ChildCategoryDto> categories) {

        return CategoryDto.builder()
            .id(category.getId())
            .categoryName(category.getCategoryName())
            .imageUrl(category.getImageUrl())
            .categories(categories)
            .build();
    }
}
