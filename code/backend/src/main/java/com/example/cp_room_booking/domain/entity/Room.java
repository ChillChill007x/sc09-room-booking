package com.example.cp_room_booking.domain.entity;

import com.example.cp_room_booking.common.BaseEntity;
import com.example.cp_room_booking.domain.enums.RoomStatus;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "rooms")
@BatchSize(size = 50)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private Integer capacity;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    /**
     * ห้องเป็นเจ้าของแถวในตารางเชื่อม ลบห้องออกจาก set แล้วแถวถูกลบตาม (orphanRemoval)
     * BatchSize ช่วยลด N+1 ตอนแสดงอุปกรณ์ของหลายห้องในหน้าเดียว
     */
    @Builder.Default
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RoomEquipment> equipment = new HashSet<>();

    /**
     * แก้รายการอุปกรณ์ให้ตรงกับที่ส่งมา แถวเดิมแก้แค่จำนวน เพื่อไม่ให้ insert แถว key ซ้ำก่อน delete
     */
    public void syncEquipment(Map<Equipment, Integer> desired) {
        Map<Long, Integer> quantityById = new HashMap<>();
        desired.forEach((item, quantity) -> quantityById.put(item.getId(), quantity));

        equipment.removeIf(existing -> !quantityById.containsKey(existing.getEquipment().getId()));
        equipment.forEach(existing -> existing.setQuantity(quantityById.get(existing.getEquipment().getId())));

        Set<Long> existingIds = new HashSet<>();
        equipment.forEach(existing -> existingIds.add(existing.getEquipment().getId()));
        desired.forEach((item, quantity) -> {
            if (!existingIds.contains(item.getId())) {
                equipment.add(new RoomEquipment(this, item, quantity));
            }
        });
    }

    public boolean isActive() {
        return status == RoomStatus.ACTIVE;
    }
}
