package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingPostDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking_WhenValid_ShouldReturnCreatedBooking() throws Exception {
        BookingPostDto postDto = new BookingPostDto();
        postDto.setItemId(1L);
        postDto.setStart(LocalDateTime.now().plusDays(1));
        postDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingOutDto responseDto = new BookingOutDto();
        responseDto.setId(1L);
        responseDto.setStatus(BookingStatus.WAITING);

        when(bookingService.create(anyLong(), any(BookingPostDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void createBooking_WhenUserNotFound_ShouldReturn404() throws Exception {
        BookingPostDto postDto = new BookingPostDto();
        postDto.setItemId(1L);
        postDto.setStart(LocalDateTime.now().plusDays(1));
        postDto.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingService.create(anyLong(), any(BookingPostDto.class)))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isNotFound());
    }
}
