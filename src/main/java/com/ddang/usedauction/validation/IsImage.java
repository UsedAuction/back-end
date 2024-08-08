package com.ddang.usedauction.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// multipart 타입을 검증하기 위한 어노테이션
@Constraint(validatedBy = MultipartValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsImage {

    String message() default "Invalid Image Type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
