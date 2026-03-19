package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingMapper;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private BookingMapper bookingMapper;

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

    @Test
    void create_WhenItemNotAvailable_ShouldThrowValidationException() {
        item.setAvailable(false);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(ru.practicum.shareit.exception.BadRequestException.class,
                () -> bookingService.create(booker.getId(), bookingPostDto));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_WhenBookerIsOwner_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(ru.practicum.shareit.exception.NotFoundException.class,
                () -> bookingService.create(owner.getId(), bookingPostDto));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_WhenEndBeforeStart_ShouldThrowValidationException() {
        bookingPostDto.setStart(LocalDateTime.now().plusDays(5));
        bookingPostDto.setEnd(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(ru.practicum.shareit.exception.BadRequestException.class,
                () -> bookingService.create(booker.getId(), bookingPostDto));

        verify(bookingRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALL", "CURRENT", "FUTURE", "PAST", "WAITING", "REJECTED"})
    void getAllByBooker_ShouldWorkForAllStates(String state) {
        User mockBooker = new User();
        mockBooker.setId(1L);

        Item mockItem = new Item();
        mockItem.setId(1L);
        mockItem.setName("Test Item");

        ru.practicum.shareit.booking.model.Booking mockBooking = new ru.practicum.shareit.booking.model.Booking();
        mockBooking.setId(1L);
        mockBooking.setBooker(mockBooker);
        mockBooking.setItem(mockItem);
        mockBooking.setStart(LocalDateTime.now().minusDays(1));
        mockBooking.setEnd(LocalDateTime.now().plusDays(1));
        mockBooking.setStatus(ru.practicum.shareit.booking.model.BookingStatus.WAITING);

        ru.practicum.shareit.booking.dto.BookingOutDto mockOutDto = new ru.practicum.shareit.booking.dto.BookingOutDto();
        mockOutDto.setId(1L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockBooker));

        when(bookingMapper.toOutDto(any(ru.practicum.shareit.booking.model.Booking.class)))
                .thenReturn(mockOutDto);

        List<ru.practicum.shareit.booking.model.Booking> bookings = List.of(mockBooking);

        lenient().when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(bookings);
        lenient().when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any(), any()))
                .thenReturn(bookings);
        lenient().when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);
        lenient().when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);
        lenient().when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);

        var result = bookingService.getAllByBooker(mockBooker.getId(), state, 0, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());

        assertEquals(1L, result.get(0).getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALL", "CURRENT", "FUTURE", "PAST", "WAITING", "REJECTED"})
    void getAllByOwner_ShouldWorkForAllStates(String state) {
        User mockOwner = new User();
        mockOwner.setId(2L);

        Item mockItem = new Item();
        mockItem.setId(1L);
        mockItem.setOwner(mockOwner);

        ru.practicum.shareit.booking.model.Booking mockBooking = new ru.practicum.shareit.booking.model.Booking();
        mockBooking.setId(1L);
        mockBooking.setBooker(new User()); // кто-то забронировал
        mockBooking.setItem(mockItem);
        mockBooking.setStatus(ru.practicum.shareit.booking.model.BookingStatus.WAITING);

        ru.practicum.shareit.booking.dto.BookingOutDto mockOutDto = new ru.practicum.shareit.booking.dto.BookingOutDto();
        mockOutDto.setId(1L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockOwner));
        when(bookingMapper.toOutDto(any())).thenReturn(mockOutDto);

        lenient().when(itemRepository.existsByOwnerId(anyLong())).thenReturn(true);

        when(bookingMapper.toOutDto(any())).thenReturn(mockOutDto);

        List<ru.practicum.shareit.booking.model.Booking> bookings = List.of(mockBooking);

        lenient().when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(bookings);
        lenient().when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any(), any()))
                .thenReturn(bookings);
        lenient().when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);
        lenient().when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);
        lenient().when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);

        var result = bookingService.getAllByOwner(mockOwner.getId(), state, 0, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
