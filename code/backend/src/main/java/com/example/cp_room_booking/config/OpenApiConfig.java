package com.example.cp_room_booking.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI เปิดที่ /swagger-ui.html กดปุ่ม Authorize แล้วใส่ token จาก /api/v1/auth/login
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SC09 Room Booking API",
                version = "v1",
                description = "ระบบจองห้องอาคารวิทยวิภาส (SC09) วิทยาลัยการคอมพิวเตอร์ มหาวิทยาลัยขอนแก่น"),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
@SecurityScheme(
        name = OpenApiConfig.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
}
