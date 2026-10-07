package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.common.PageResponse;
import com.example.cp_room_booking.dto.request.RoomSearchRequest;
import com.example.cp_room_booking.dto.response.RoomResponse;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.service.RoomService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

    private static final String VALID_ROOM = """
            {"code":"SC09-5501","name":"ห้องใหม่","floor":5,"capacity":30,"roomTypeId":1}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @Test
    void search_passesPageSortAndFiltersToService() throws Exception {
        when(roomService.search(any(), any())).thenReturn(emptyPage());

        mockMvc.perform(get("/api/v1/rooms")
                        .param("page", "1").param("size", "5").param("sort", "capacity,desc")
                        .param("floor", "2").param("minCapacity", "40").param("keyword", "lab"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        ArgumentCaptor<RoomSearchRequest> criteria = ArgumentCaptor.forClass(RoomSearchRequest.class);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(roomService).search(criteria.capture(), pageable.capture());
        assertThat(criteria.getValue().floor()).isEqualTo(2);
        assertThat(criteria.getValue().minCapacity()).isEqualTo(40);
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageable.getValue().getSort().getOrderFor("capacity").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void search_withoutParams_usesDefaultSortByCode() throws Exception {
        when(roomService.search(any(), any())).thenReturn(emptyPage());

        mockMvc.perform(get("/api/v1/rooms")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(roomService).search(any(), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageable.getValue().getSort().getOrderFor("code")).isNotNull();
    }

    @Test
    void available_missingStart_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/rooms/available").param("end", "2030-01-15T11:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_validRoom_returns201() throws Exception {
        when(roomService.create(any())).thenReturn(RoomResponse.builder().id(20L).code("SC09-5501").build());

        mockMvc.perform(post("/api/v1/rooms").contentType(MediaType.APPLICATION_JSON).content(VALID_ROOM))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SC09-5501"));
    }

    @Test
    void create_invalidCapacity_returns400() throws Exception {
        String body = """
                {"code":"SC09-5501","name":"ห้องใหม่","floor":5,"capacity":0,"roomTypeId":1}
                """;

        mockMvc.perform(post("/api/v1/rooms").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("capacity"));
    }

    @Test
    void create_duplicateCode_returns409() throws Exception {
        when(roomService.create(any())).thenThrow(new ConflictException("รหัสห้องซ้ำ"));

        mockMvc.perform(post("/api/v1/rooms").contentType(MediaType.APPLICATION_JSON).content(VALID_ROOM))
                .andExpect(status().isConflict());
    }

    private PageResponse<RoomResponse> emptyPage() {
        return PageResponse.<RoomResponse>builder().content(List.of()).page(0).size(10).totalElements(0)
                .totalPages(0).first(true).last(true).build();
    }
}
