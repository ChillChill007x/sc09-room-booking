package com.example.cp_room_booking.domain.entity;

import com.example.cp_room_booking.common.BaseEntity;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, length = 300)
    private String purpose;

    @Column(nullable = false)
    private Integer attendees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    /**
     * history ถูกบันทึกพร้อม booking ผ่าน cascade PERSIST แต่ไม่ cascade การลบ
     * การลบ booking ให้ฐานข้อมูลลบ history ตาม (ON DELETE CASCADE)
     */
    @Builder.Default
    @OrderBy("changedAt ASC, id ASC")
    @OneToMany(mappedBy = "booking", cascade = CascadeType.PERSIST)
    private List<BookingStatusHistory> history = new ArrayList<>();

    /**
     * เปลี่ยนสถานะพร้อมบันทึกประวัติทุกครั้ง changedBy เป็น null ได้เมื่อระบบเปลี่ยนเอง (scheduler)
     */
    public void changeStatus(BookingStatus newStatus, User changedBy, String note, LocalDateTime changedAt) {
        BookingStatus previous = this.status;
        this.status = newStatus;
        history.add(BookingStatusHistory.builder()
                .booking(this)
                .fromStatus(previous)
                .toStatus(newStatus)
                .changedBy(changedBy)
                .note(note)
                .changedAt(changedAt)
                .build());
    }

    public boolean isOwnedBy(Long userId) {
        return user.getId().equals(userId);
    }
}
