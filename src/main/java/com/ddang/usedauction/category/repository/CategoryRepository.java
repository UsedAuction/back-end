package com.ddang.usedauction.category.repository;

import com.ddang.usedauction.category.domain.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIdIsNull();

    List<Category> findByParentId(Long parentId);
}
