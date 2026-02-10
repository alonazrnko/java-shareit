package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Failed to add item: User with id {} not found", userId);
                    return new NotFoundException("User not found with id: " + userId);
                });

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));

        if (!existingItem.getOwner().getId().equals(userId)) {
            log.error("User {} is not the owner of item {}", userId, itemId);
            throw new NotFoundException("Item not found for this user");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        log.info("Updating item id: {} for user: {}", itemId, userId);
        return ItemMapper.toItemDto(itemRepository.save(existingItem));
    }

    @Override
    public ItemDto getItem(Long userId, Long itemId) {
        if (!userRepository.existsById(userId)) {
            log.error("Failed to get item: User with id {} not found", userId);
            throw new NotFoundException("User not found with id: " + userId);
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Item with id {} not found", itemId);
                    return new NotFoundException("Item not found with id: " + itemId);
                });

        log.info("Item id: {} successfully retrieved for user id: {}", itemId, userId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getUserItems(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.error("Failed to retrieve items: User with id {} not found", userId);
            throw new NotFoundException("User not found with id: " + userId);
        }

        return itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
