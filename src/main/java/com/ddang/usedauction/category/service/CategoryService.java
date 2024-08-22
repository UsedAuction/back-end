package com.ddang.usedauction.category.service;

import com.ddang.usedauction.category.domain.Category;
import com.ddang.usedauction.category.dto.CategoryByParentDto;
import com.ddang.usedauction.category.dto.ChildCategoryDto;
import com.ddang.usedauction.category.repository.CategoryRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;

  public List<Category> getAllParentCategories() {
    return categoryRepository.findAllParentCategories();
  }

  public CategoryByParentDto getCategoriesByParentId(Long parentId) {
    Category parentCategory = categoryRepository.findById(parentId)
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 카테고리입니다."));

    List<ChildCategoryDto> childCategories = categoryRepository.findCategoriesByParentId(parentId)
        .stream()
        .map(ChildCategoryDto::from)
        .collect(Collectors.toList());

    return CategoryByParentDto.builder()
        .id(parentCategory.getId())
        .categoryName(parentCategory.getCategoryName())
        .categories(childCategories)
        .build();
  }
}