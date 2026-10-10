package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.exception.ForbiddenOperationException;
import com.example.cp_room_booking.service.NotificationService;
import com.example.cp_room_booking.support.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @BeforeEach
    void login() {
        SecurityTestUtils.loginAs(5L, Role.STUDENT);
    }

    @AfterEach
    void logout() {
        SecurityTestUtils.logout();
    }

    @Test
    void markRead_otherUsersNotification_returns403() throws Exception {
        when(notificationService.markRead(eq(10L), any()))
                .thenThrow(new ForbiddenOperationException("จัดการได้เฉพาะแจ้งเตือนของตัวเอง"));

        mockMvc.perform(patch("/api/v1/notifications/10/read"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void delete_otherUsersNotification_returns403() throws Exception {
        doThrow(new ForbiddenOperationException("ไม่ใช่ของคุณ")).when(notificationService).delete(eq(10L), any());

        mockMvc.perform(delete("/api/v1/notifications/10")).andExpect(status().isForbidden());
    }

    @Test
    void unreadCount_usesLoggedInUser() throws Exception {
        when(notificationService.countUnread(5L)).thenReturn(3L);

        mockMvc.perform(get("/api/v1/users/me/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
        verify(notificationService).countUnread(5L);
    }

    @Test
    void delete_ownNotification_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/notifications/11")).andExpect(status().isNoContent());
    }
}
