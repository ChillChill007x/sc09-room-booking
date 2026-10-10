package com.example.cp_room_booking.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * เปิด @Scheduled เฉพาะเมื่อ app.scheduler.enabled=true (ปิดไว้ตอนรัน test)
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true")
public class SchedulingConfig {
}
