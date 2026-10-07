package com.example.cp_room_booking.domain.enums;

public enum Role {
    STUDENT,
    LECTURER,
    STAFF,
    ADMIN;

    public boolean isStaff() {
        return this == STAFF || this == ADMIN;
    }
}
