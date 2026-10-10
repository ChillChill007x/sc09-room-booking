package com.example.cp_room_booking.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Primary key แบบผสมของตารางเชื่อม room_equipment
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RoomEquipmentId implements Serializable {

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "equipment_id")
    private Long equipmentId;
}
