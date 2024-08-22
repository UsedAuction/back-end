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
public class ChildCategoryDto {

  private long id;
  private String categoryName;
  private Long parentId;

  public static ChildCategoryDto from(Category category) {

    return ChildCategoryDto.builder()
        .id(category.getId())
        .categoryName(category.getCategoryName())
        .parentId(category.getParentId())
        .build();
  }
}
