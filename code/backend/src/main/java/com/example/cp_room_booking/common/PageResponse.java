package com.example.cp_room_booking.common;

import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * รูปแบบผลลัพธ์แบบแบ่งหน้าที่ทุก endpoint ใช้เหมือนกัน
 */
@Builder
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
