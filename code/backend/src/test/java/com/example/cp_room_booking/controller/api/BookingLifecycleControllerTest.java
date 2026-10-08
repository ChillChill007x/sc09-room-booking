package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.exception.ForbiddenOperationException;
import com.example.cp_room_booking.exception.InvalidBookingStateException;
import com.example.cp_room_booking.service.BookingLifecycleService;
import com.example.cp_room_booking.support.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingLifecycleController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingLifecycleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingLifecycleService lifecycleService;

    @BeforeEach
    void login() {
        SecurityTestUtils.loginAs(2L, Role.STAFF);
    }

    @AfterEach
    void logout() {
        SecurityTestUtils.logout();
    }

    @Test
    void approve_returns200WithNewStatus() throws Exception {
        when(lifecycleService.changeStatus(eq(100L), eq(BookingAction.APPROVE), any(), any()))
                .thenReturn(BookingResponse.builder().id(100L).status(BookingStatus.APPROVED).build());

        mockMvc.perform(patch("/api/v1/bookings/100/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"APPROVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void unknownAction_returns400() throws Exception {
        mockMvc.perform(patch("/api/v1/bookings/100/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"FLY\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidState_returns409() throws Exception {
        when(lifecycleService.changeStatus(eq(100L), any(), any(), any()))
                .thenThrow(new InvalidBookingStateException(BookingStatus.REJECTED, BookingAction.APPROVE));

        mockMvc.perform(patch("/api/v1/bookings/100/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"APPROVE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void forbidden_returns403() throws Exception {
        when(lifecycleService.changeStatus(eq(100L), any(), any(), any()))
                .thenThrow(new ForbiddenOperationException("ไม่มีสิทธิ์"));

        mockMvc.perform(patch("/api/v1/bookings/100/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"CHECK_IN\"}"))
                .andExpect(status().isForbidden());
    }
}
