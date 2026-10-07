package com.example.cp_room_booking.repository.specification;

import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.entity.RoomEquipment;
import com.example.cp_room_booking.domain.enums.RoomStatus;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

/**
 * ตัวกรองห้องแบบประกอบกันได้ ใช้กับ GET /rooms และการค้นหาห้องว่าง
 */
public final class RoomSpecifications {

    private RoomSpecifications() {
    }

    public static Specification<Room> hasFloor(Integer floor) {
        return (root, query, cb) -> cb.equal(root.get("floor"), floor);
    }

    public static Specification<Room> hasType(Long typeId) {
        return (root, query, cb) -> cb.equal(root.get("roomType").get("id"), typeId);
    }

    public static Specification<Room> minCapacity(Integer capacity) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("capacity"), capacity);
    }

    public static Specification<Room> hasStatus(RoomStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Room> notStatus(RoomStatus status) {
        return (root, query, cb) -> cb.notEqual(root.get("status"), status);
    }

    public static Specification<Room> keyword(String keyword) {
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("code")), pattern),
                cb.like(cb.lower(root.get("name")), pattern));
    }

    /**
     * ใช้ subquery แทน join เพื่อไม่ให้ห้องซ้ำในผลลัพธ์และ count ของ Page ถูกต้อง
     */
    public static Specification<Room> hasEquipment(Long equipmentId) {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<RoomEquipment> link = sub.from(RoomEquipment.class);
            sub.select(link.get("id").get("equipmentId"))
                    .where(cb.equal(link.get("room"), root),
                            cb.equal(link.get("equipment").get("id"), equipmentId));
            return cb.exists(sub);
        };
    }

    public static Specification<Room> idNotIn(Collection<Long> ids) {
        return (root, query, cb) -> cb.not(root.get("id").in(ids));
    }
}
