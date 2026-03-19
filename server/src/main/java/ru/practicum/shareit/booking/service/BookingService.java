package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.dto.BookingPostDto;

import java.util.List;

public interface BookingService {
    BookingOutDto create(Long userId, BookingPostDto dto);

    BookingOutDto approve(Long userId, Long bookingId, Boolean approved);

    BookingOutDto getById(Long userId, Long bookingId);

    List<BookingOutDto> getAllByBooker(Long userId, String state, Integer from, Integer size);

    List<BookingOutDto> getAllByOwner(Long userId, String state, Integer from, Integer size);
}
