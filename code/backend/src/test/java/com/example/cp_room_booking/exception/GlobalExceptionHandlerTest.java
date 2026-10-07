package com.example.cp_room_booking.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ตรวจว่า GlobalExceptionHandler คืน ErrorResponse ครบทุก field และ status code ถูกต้อง
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FakeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void notFound_returns404WithErrorFormat() throws Exception {
        mockMvc.perform(get("/fake/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("ไม่พบห้อง id=1"))
                .andExpect(jsonPath("$.path").value("/fake/not-found"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void conflict_returns409() throws Exception {
        mockMvc.perform(get("/fake/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void businessRule_returns400() throws Exception {
        mockMvc.perform(get("/fake/business"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ผิดกฎ"));
    }

    @Test
    void forbidden_returns403() throws Exception {
        mockMvc.perform(get("/fake/forbidden"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validation_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/fake/validate").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("ต้องมีชื่อ"));
    }

    @Test
    void unexpected_returns500WithoutLeakingDetail() throws Exception {
        mockMvc.perform(get("/fake/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("เกิดข้อผิดพลาดภายในระบบ"));
    }

    record FakeRequest(@NotBlank(message = "ต้องมีชื่อ") String name) {
    }

    @RestController
    static class FakeController {

        @GetMapping("/fake/not-found")
        void notFound() {
            throw ResourceNotFoundException.of("ห้อง", 1);
        }

        @GetMapping("/fake/conflict")
        void conflict() {
            throw new ConflictException("ชนกัน");
        }

        @GetMapping("/fake/business")
        void business() {
            throw new BusinessRuleException("ผิดกฎ");
        }

        @GetMapping("/fake/forbidden")
        void forbidden() {
            throw new ForbiddenOperationException("ไม่ใช่ของคุณ");
        }

        @PostMapping("/fake/validate")
        void validate(@Valid @RequestBody FakeRequest request) {
        }

        @GetMapping("/fake/boom")
        void boom() {
            throw new IllegalStateException("secret detail");
        }
    }
}
