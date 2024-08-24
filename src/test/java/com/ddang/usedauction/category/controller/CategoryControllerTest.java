package com.ddang.usedauction.category.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.category.dto.CategoryByParentDto;
import com.ddang.usedauction.category.dto.ParentCategoryDto;
import com.ddang.usedauction.category.service.CategoryService;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.token.service.RefreshTokenService;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

  MockMvc mockMvc;

  @MockBean
  private CategoryService categoryService;

  @MockBean
  TokenProvider tokenProvider;

  @MockBean
  RefreshTokenService refreshTokenService;

  @BeforeEach
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(categoryService)).build();
  }

  @Test
  @DisplayName("모든 부모 카테고리 조회 API")
  public void findAllParentCategories() throws Exception {
    ParentCategoryDto parentCategory1 = ParentCategoryDto.builder()
        .id(1L)
        .categoryName("Electronics")
        .imageUrl("url1")
        .build();

    ParentCategoryDto parentCategory2 = ParentCategoryDto.builder()
        .id(2L)
        .categoryName("Books")
        .imageUrl("url2")
        .build();

    when(categoryService.findAllParentCategories())
        .thenReturn(Arrays.asList(parentCategory1, parentCategory2));

    mockMvc.perform(get("/api/categories")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].categoryName").value("Electronics"))
        .andExpect(jsonPath("$[0].imageUrl").value("url1"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].categoryName").value("Books"))
        .andExpect(jsonPath("$[1].imageUrl").value("url2"))
        .andDo(print());
  }

  @Test
  @DisplayName("특정 부모 카테고리와 해당하는 자식 카테고리들 찾는 API")
  public void findChildrenByParentId() throws Exception {
    CategoryByParentDto dto = CategoryByParentDto.builder()
        .id(1L)
        .categoryName("Books")
        .categories(Collections.EMPTY_LIST)
        .build();

    when(categoryService.findCategoriesByParentId(1L)).thenReturn(dto);

    mockMvc.perform(get("/api/categories/1")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.categoryName").value("Books"))
        .andExpect(jsonPath("$.categories").isArray())
        .andDo(print());
  }
}
