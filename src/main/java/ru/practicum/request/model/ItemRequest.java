package ru.practicum.request.model;

import lombok.Data;
import ru.practicum.user.model.User;
import java.time.LocalDateTime;

@Data
public class ItemRequest {
    private Long id;
    private String description;
    private User requestor;
    private LocalDateTime created;
}
