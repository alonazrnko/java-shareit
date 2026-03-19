package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingPostDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private BookingPostDto bookingPostDto;

    @BeforeEach
    void setUp() {
        booker = new User();
        booker.setId(1L);

        owner = new User();
        owner.setId(2L);

        item = new Item();
        item.setId(1L);
        item.setOwner(owner);
        item.setAvailable(true);

        bookingPostDto = new BookingPostDto();
        bookingPostDto.setItemId(1L);
        bookingPostDto.setStart(LocalDateTime.now().plusDays(1));
        bookingPostDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    void createBooking_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(1L, bookingPostDto));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_WhenItemNotAvailable_ShouldThrowValidationException() {
        item.setAvailable(false);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> bookingService.create(1L, bookingPostDto));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_WhenBookerIsOwner_ShouldThrowNotFoundException() {
        item.setOwner(booker);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.create(1L, bookingPostDto));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approveBooking_WhenUserIsNotOwner_ShouldThrowNotFoundException() {
        ru.practicum.shareit.booking.model.Booking existingBooking = new ru.practicum.shareit.booking.model.Booking();
        existingBooking.setId(1L);
        existingBooking.setItem(item);
        existingBooking.setStatus(ru.practicum.shareit.booking.model.BookingStatus.WAITING);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(existingBooking));

        assertThrows(BadRequestException.class, () -> bookingService.approve(1L, 1L, true));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_WhenStatusAlreadyApproved_ShouldThrowValidationException() {
        ru.practicum.shareit.booking.model.Booking existingBooking = new ru.practicum.shareit.booking.model.Booking();
        existingBooking.setId(1L);
        existingBooking.setItem(item); // Владелец ID = 2
        existingBooking.setStatus(ru.practicum.shareit.booking.model.BookingStatus.APPROVED);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(existingBooking));

        assertThrows(BadRequestException.class, () -> bookingService.approve(2L, 1L, true));

        verify(bookingRepository, never()).save(any());
    }
}
