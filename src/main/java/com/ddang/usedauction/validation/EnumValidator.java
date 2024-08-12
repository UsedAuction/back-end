package com.ddang.usedauction.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

// Enum 검증할 때 사용할 validator
public class EnumValidator implements ConstraintValidator<IsEnum, Enum> {

    @Override
    public boolean isValid(Enum value, ConstraintValidatorContext context) {

        if(value == null){
            return false;
        }

        Class<?> reflectionEnumClass = value.getDeclaringClass();

        return Arrays.asList(reflectionEnumClass.getEnumConstants()).contains(value);
    }
}