package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto addItem(@RequestHeader(USER_ID_HEADER) Long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        log.info("Received request to add item for user id: {}", userId);
        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID_HEADER) Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        log.info("Received request to update item id: {} for user id: {}", itemId, userId);
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader(USER_ID_HEADER) Long userId,
                           @PathVariable Long itemId) {
        log.info("Received request to get item id: {} from user id: {}", itemId, userId);
        return itemService.getItem(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Received request to get all items for user id: {}", userId);
        return itemService.getUserItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("Received request to search items with text: {}", text);
        return itemService.searchItems(text);
    }
}
