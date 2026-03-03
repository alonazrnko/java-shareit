package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingOutDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
    private UserDtoShort booker; // Nested short version
    private ItemDtoShort item;   // Nested short version

    @Data
    @AllArgsConstructor
    public static class UserDtoShort {
        private Long id;
    }

    @Data
    @AllArgsConstructor
    public static class ItemDtoShort {
        private Long id;
        private String name;
    }
}
