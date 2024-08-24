package com.ddang.usedauction.category.dto;

import com.ddang.usedauction.category.domain.Category;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class ParentCategoryDto {

  private Long id;
  private String categoryName;
  private String imageUrl;

  public static ParentCategoryDto from(Category category) {

    return ParentCategoryDto.builder()
        .id(category.getId())
        .categoryName(category.getCategoryName())
        .imageUrl(category.getImageUrl())
        .build();
  }
}
