package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(message = "Item name cannot be empty")
    private String name;

    @NotBlank(message = "Item description cannot be empty")
    private String description;

    @NotNull(message = "Availability status is required")
    private Boolean available;

    private Long requestId;
}
