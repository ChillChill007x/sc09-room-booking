package com.example.cp_room_booking.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ตารางเชื่อม Many-to-Many ระหว่างห้องกับอุปกรณ์ ที่มีข้อมูลเพิ่มคือจำนวน (quantity)
 * จึงต้องเป็น Entity ของตัวเอง ใช้ @ManyToMany ตรง ๆ ไม่ได้
 */
@Entity
@Table(name = "room_equipment")
@Getter
@Setter
@NoArgsConstructor
public class RoomEquipment {

    @EmbeddedId
    private RoomEquipmentId id;

    @MapsId("roomId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    @MapsId("equipmentId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @Column(nullable = false)
    private int quantity;

    public RoomEquipment(Room room, Equipment equipment, int quantity) {
        this.id = new RoomEquipmentId(room.getId(), equipment.getId());
        this.room = room;
        this.equipment = equipment;
        this.quantity = quantity;
    }
}
