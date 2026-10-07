package com.example.cp_room_booking.support;

import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.security.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * ตั้งผู้ใช้ที่ login ให้ test ของ Controller ที่ปิด security filter ไว้ (@AutoConfigureMockMvc(addFilters = false))
 */
public final class SecurityTestUtils {

    private SecurityTestUtils() {
    }

    public static UserPrincipal principal(long id, Role role) {
        return new UserPrincipal(id, "user" + id + "@kkumail.com", "hash", role, true);
    }

    public static UserPrincipal loginAs(long id, Role role) {
        UserPrincipal principal = principal(id, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        return principal;
    }

    public static void logout() {
        SecurityContextHolder.clearContext();
    }
}
