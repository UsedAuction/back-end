package com.ddang.usedauction.category.service;

import com.ddang.usedauction.category.domain.Category;
import com.ddang.usedauction.category.dto.CategoryDto;
import com.ddang.usedauction.category.repository.CategoryRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    CacheManager cacheManager;

    @InjectMocks
    CategoryService categoryService;

    Category parentCategory1;
    Category parentCategory2;
    List<Category> childCategories1;
    List<Category> childCategories2;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

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
    void testGetAllCategoriesExecutionTime() {
        // 실행 시간 측정 시작
        long startTime = System.nanoTime();

        List<CategoryDto> categories = categoryService.getAllCategories();
        // 실행 시간 측정 종료
        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000;  // 밀리초로 변환

        System.out.println("카테고리 조회 시간 " + executionTime + " ms");

        long startTime2 = System.nanoTime();

        List<CategoryDto> categories2 = categoryService.getAllCategories();

        long endTime2 = System.nanoTime();
        long executionTime2 = (endTime2 - startTime2) / 1_000_000;  // 밀리초로 변환

        System.out.println("카테고리 조회 시간 " + executionTime2 + " ms");

    }

}