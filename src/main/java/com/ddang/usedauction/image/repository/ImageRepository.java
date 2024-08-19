package com.ddang.usedauction.image.repository;

import com.ddang.usedauction.image.domain.Image;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findByImageName(String imageName); // 이미지 이름을 통해 조회
}
