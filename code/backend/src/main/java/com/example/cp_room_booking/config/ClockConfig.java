package com.example.cp_room_booking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Clock กลางของระบบ ทุก Service ที่ต้องรู้เวลาปัจจุบันรับ Clock ผ่าน constructor เพื่อให้ test กำหนดเวลาได้
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${app.zone-id}") String zoneId) {
        return Clock.system(ZoneId.of(zoneId));
    }
}
