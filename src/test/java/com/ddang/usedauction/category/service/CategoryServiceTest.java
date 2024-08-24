package com.ddang.usedauction.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.category.domain.Category;
import com.ddang.usedauction.category.dto.CategoryByParentDto;
import com.ddang.usedauction.category.dto.ChildCategoryDto;
import com.ddang.usedauction.category.repository.CategoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  CategoryRepository categoryRepository;

  @InjectMocks
  CategoryService categoryService;

  Category parentCategory1;
  Category parentCategory2;
  List<Category> childCategories1;
  List<Category> childCategories2;


  @BeforeEach
  void setUp() {
    parentCategory1 = Category.builder()
        .id(1L)
        .categoryName("parent Category1")
        .parentId(null)
        .build();

    Category child1 = Category.builder()
        .id(3L)
        .categoryName("child Category1-1")
        .parentId(1L)
        .build();

    Category child2 = Category.builder()
        .id(4L)
        .categoryName("child Category1-2")
        .parentId(1L)
        .build();

    parentCategory2 = Category.builder()
        .id(2L)
        .categoryName("parent Category2")
        .parentId(null)
        .build();

    Category child3 = Category.builder()
        .id(5L)
        .categoryName("child Category2-1")
        .parentId(2L)
        .build();

    Category child4 = Category.builder()
        .id(6L)
        .categoryName("child Category2-2")
        .parentId(21L)
        .build();

    childCategories1 = new ArrayList<>();
    childCategories1.add(child1);
    childCategories1.add(child2);

    childCategories2 = new ArrayList<>();
    childCategories2.add(child3);
    childCategories2.add(child4);
  }

  @Test
  @DisplayName("부모 카테고리들 찾기")
  void findParentCategories() {
    List<Category> parentCategories = new ArrayList<>();
    parentCategories.add(parentCategory1);
    parentCategories.add(parentCategory2);

    when(categoryRepository.findAllParentCategories()).thenReturn(parentCategories);

    List<Category> result = categoryRepository.findAllParentCategories();

    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getCategoryName()).isEqualTo("parent Category1");
    assertThat(result.get(1).getCategoryName()).isEqualTo("parent Category2");
  }

  @Test
  @DisplayName("특정 부모 카테고리와 해당하는 자식 카테고리들 찾기")
  void findCategoriesByParentId() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory1));
    when(categoryRepository.findCategoriesByParentId(1L)).thenReturn(childCategories1);

    CategoryByParentDto result = categoryService.findCategoriesByParentId(1L);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getCategoryName()).isEqualTo("parent Category1");
    assertThat(result.getCategories()).hasSize(2);

    List<ChildCategoryDto> children = result.getCategories();
    assertThat(children.get(0).getCategoryName()).isEqualTo("child Category1-1");
    assertThat(children.get(1).getCategoryName()).isEqualTo("child Category1-2");
  }

  @Test
  @DisplayName("특정 부모 카테고리와 해당하는 자식 카테고리들 찾기 - 실패 (부모카테고리 존재x)")
  void findCategoriesByParentId_NOTExist() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> categoryService.findCategoriesByParentId(1L));
  }
}