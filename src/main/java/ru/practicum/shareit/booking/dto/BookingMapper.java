package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Component;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Component
public class BookingMapper {

    // Map fields from PostDto to Entity
    public Booking toBooking(BookingPostDto dto, Item item, User booker) {
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        return booking;
    }

    // Map Entity to OutDto for API response
    public BookingOutDto toOutDto(Booking booking) {
        BookingOutDto dto = new BookingOutDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());

        // Map nested booker id only
        dto.setBooker(new BookingOutDto.UserDtoShort(
                booking.getBooker().getId()
        ));

        // Map nested item id and name
        dto.setItem(new BookingOutDto.ItemDtoShort(
                booking.getItem().getId(),
                booking.getItem().getName()
        ));

        return dto;
    }
}