package com.ddang.usedauction.category.repository;

import com.ddang.usedauction.category.domain.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  @Query("SELECT c FROM Category c WHERE c.parentId IS NULL")
  List<Category> findAllParentCategories();

  List<Category> findCategoriesByParentId(Long parentId);
}
