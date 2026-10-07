package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.entity.UserProfile;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.domain.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByEmail_seededUser_loadsProfile() {
        User user = userRepository.findByEmail("student@kkumail.com").orElseThrow();

        assertThat(user.getRole()).isEqualTo(Role.STUDENT);
        assertThat(user.getProfile().getStudentCode()).isEqualTo("6733800001");
    }

    @Test
    void save_userWithProfile_cascadesOneToOne() {
        User user = User.builder()
                .email("cascade@kkumail.com")
                .passwordHash("hash")
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .build();
        user.attachProfile(UserProfile.builder().fullName("Cascade Test").build());

        Long id = userRepository.saveAndFlush(user).getId();
        entityManager.clear();

        User found = userRepository.findWithProfileById(id).orElseThrow();
        assertThat(found.getProfile().getId()).isNotNull();
        assertThat(found.getProfile().getFullName()).isEqualTo("Cascade Test");
    }

    @Test
    void existsByEmail_checksUniqueEmail() {
        assertThat(userRepository.existsByEmail("staff@kkumail.com")).isTrue();
        assertThat(userRepository.existsByEmail("nobody@kkumail.com")).isFalse();
    }

    @Test
    void findAllByRoleInAndStatus_returnsActiveStaff() {
        List<User> staff = userRepository.findAllByRoleInAndStatus(Set.of(Role.STAFF, Role.ADMIN), UserStatus.ACTIVE);

        assertThat(staff).extracting(User::getEmail)
                .containsExactlyInAnyOrder("staff@kkumail.com", "admin@kkumail.com");
    }
}
