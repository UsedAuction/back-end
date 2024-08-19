package com.ddang.usedauction.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

// IsImage 어노테이션에서 사용할 validator
public class MultipartValidator implements ConstraintValidator<IsImage, MultipartFile> {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg",
        "png"); // 허용할 확장자 목록

    @Override
    public boolean isValid(MultipartFile multipartFile,
        ConstraintValidatorContext context) {

        String fileName = multipartFile.getOriginalFilename();
        if (fileName == null) { // 이름과 확장자 모두 없는 경우
            return false;
        }

        String extension = getExtension(fileName);

        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * 파일 확장자 반환 메소드
     *
     * @param fileName 전체 파일 이름
     * @return 파일 확장자
     */
    private static String getExtension(String fileName) {

        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot == -1) { // 확장자가 없는 경우
            return "";
        }

        return fileName.substring(lastIndexOfDot + 1);
    }
}
