package com.example.cp_room_booking.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * จำกัดความยาวเป็นจำนวนไบต์ UTF-8 ไม่ใช่จำนวนตัวอักษร
 * ใช้กับรหัสผ่านเพราะ BCrypt รับได้ไม่เกิน 72 ไบต์ (อักษรไทย 1 ตัว = 3 ไบต์)
 */
@Documented
@Constraint(validatedBy = MaxUtf8BytesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxUtf8Bytes {

    int value();

    String message() default "ข้อความยาวเกินกำหนด";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
