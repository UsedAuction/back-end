package com.ddang.usedauction.category.controller;

import com.ddang.usedauction.category.dto.CategoryByParentDto;
import com.ddang.usedauction.category.dto.ParentCategoryDto;
import com.ddang.usedauction.category.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/categories")
@RequiredArgsConstructor
@RestController
public class CategoryController {

  private final CategoryService categoryService;

  /**
   * 대분류 카테고리 클릭 시 대분류에 포함된 모든 상품 조회
   */
  @GetMapping
  public ResponseEntity<List<ParentCategoryDto>> findAllParentCategories() {
    return ResponseEntity.ok(categoryService.findAllParentCategories());
  }

  /**
   * 하나의 대분류 카테고리와 그 대분류에 속하는 자식 카테고리들 조회
   *
   * @param parentId 부모 카테고리 Id
   */
  @GetMapping("/{parentId}")
  public ResponseEntity<CategoryByParentDto> findChildrenByParentId(
      @PathVariable("parentId") Long parentId) {
    return ResponseEntity.ok(categoryService.findCategoriesByParentId(parentId));
  }
}