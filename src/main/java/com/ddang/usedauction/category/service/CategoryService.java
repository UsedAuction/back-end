package com.ddang.usedauction.category.service;

import com.ddang.usedauction.category.domain.Category;
import com.ddang.usedauction.category.dto.CategoryDto;
import com.ddang.usedauction.category.dto.ChildCategoryDto;
import com.ddang.usedauction.category.repository.CategoryRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Cacheable(value = "categories", key = "'allCategories'")
    public List<CategoryDto> getAllCategories() {
        List<Category> parentCategories = categoryRepository.findByParentIdIsNull();

        return parentCategories.stream()
            .map(category -> {
                List<ChildCategoryDto> childCategories = getChildCategories(category.getId());

                return CategoryDto.from(category, childCategories);
            })
            .collect(Collectors.toList());
    }

    private List<ChildCategoryDto> getChildCategories(Long parentId) {
        List<Category> childCategories = categoryRepository.findByParentId(parentId);

        return childCategories.stream()
            .map(ChildCategoryDto::from)
            .collect(Collectors.toList());
    }


}