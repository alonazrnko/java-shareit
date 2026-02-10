package ru.practicum.booking.model;

import lombok.Data;
import ru.practicum.item.model.Item;
import ru.practicum.user.model.User;
import java.time.LocalDateTime;

@Data
public class Booking {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Item item;
    private User booker;
    private String status;
}