package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "profile")
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "profile")
    Optional<User> findWithProfileById(Long id);

    @Override
    @EntityGraph(attributePaths = "profile")
    Page<User> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "profile")
    Page<User> findAllByRole(Role role, Pageable pageable);

    /**
     * ใช้หาเจ้าหน้าที่ที่ต้องได้รับแจ้งเตือนเมื่อมีการจองใหม่
     */
    List<User> findAllByRoleInAndStatus(Collection<Role> roles, UserStatus status);
}
