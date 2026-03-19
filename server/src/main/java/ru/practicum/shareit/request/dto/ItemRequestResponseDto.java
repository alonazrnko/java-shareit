package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemRequestResponseDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemForRequestDto> items;

    @Data
    @Builder
    public static class ItemForRequestDto {
        private Long id;
        private String name;
        private String description;
        private Boolean available;
        private Long requestId;
        private Long ownerId;
    }
}
