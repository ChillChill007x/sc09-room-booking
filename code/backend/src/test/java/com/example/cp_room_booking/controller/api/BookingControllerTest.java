package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.service.BookingService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    private static final String VALID_BODY = """
            {"roomId":7,"startTime":"2030-01-15T09:00:00","endTime":"2030-01-15T11:00:00",
             "purpose":"ติวหนังสือ","attendees":10}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @BeforeEach
    void login() {
        SecurityTestUtils.loginAs(4L, Role.STUDENT);
    }

    @AfterEach
    void logout() {
        SecurityTestUtils.logout();
    }

    @Test
    void create_validBooking_returns201() throws Exception {
        when(bookingService.create(any(), any())).thenReturn(BookingResponse.builder()
                .id(100L).roomId(7L).status(BookingStatus.PENDING).build());

        mockMvc.perform(post("/api/v1/bookings").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void create_missingFields_returns400() throws Exception {
        String body = """
                {"roomId":7,"purpose":"","attendees":0}
                """;

        mockMvc.perform(post("/api/v1/bookings").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(4));
        verifyNoInteractions(bookingService);
    }

    @Test
    void create_timeConflict_returns409() throws Exception {
        when(bookingService.create(any(), any())).thenThrow(new ConflictException("ช่วงเวลานี้มีการจองห้องนี้แล้ว"));

        mockMvc.perform(post("/api/v1/bookings").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("ช่วงเวลานี้มีการจองห้องนี้แล้ว"));
    }

    @Test
    void roomSchedule_invalidDate_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/rooms/7/bookings").param("date", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void roomSchedule_validDate_returns200() throws Exception {
        when(bookingService.findRoomSchedule(eq(7L), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/rooms/7/bookings").param("date", "2030-01-15"))
                .andExpect(status().isOk());
    }
}
