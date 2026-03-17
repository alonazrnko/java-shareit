package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        if (itemDto.getRequestId() != null) {
            if (!itemRequestRepository.existsById(itemDto.getRequestId())) {
                throw new NotFoundException("Item request not found");
            }
        }

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("User is not the owner of this item");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        ItemDto dto = itemMapper.toItemDto(item);
        LocalDateTime now = LocalDateTime.now();

        dto.setComments(commentRepository.findAllByItemId(itemId).stream()
                .map(itemMapper::toCommentDto)
                .collect(Collectors.toList()));

        if (item.getOwner().getId().equals(userId)) {
            enrichItemDto(dto, now);
        }

        return dto;
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        List<Item> items = itemRepository.findByOwnerId(userId);
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        LocalDateTime now = LocalDateTime.now();

        Map<Long, List<CommentDto>> commentsMap = commentRepository.findAllByItemIdIn(itemIds).stream()
                .map(itemMapper::toCommentDto)
                .collect(Collectors.groupingBy(CommentDto::getItemId));

        return items.stream()
                .map(itemMapper::toItemDto)
                .map(dto -> {
                    dto.setComments(commentsMap.getOrDefault(dto.getId(), new ArrayList<>()));
                    return enrichItemDto(dto, now);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.search(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        LocalDateTime now = LocalDateTime.now();

        boolean hasBooking = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, now, BookingStatus.APPROVED);

        if (!hasBooking) {
            throw new BadRequestException("User hasn't rented this item or booking is not finished");
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);

        return itemMapper.toCommentDto(commentRepository.save(comment));
    }

    private ItemDto enrichItemDto(ItemDto dto, LocalDateTime now) {
        Booking last = bookingRepository
                .findFirstByItemIdAndStartBeforeAndStatusNotOrderByStartDesc(
                        dto.getId(), now, BookingStatus.REJECTED);

        Booking next = bookingRepository
                .findFirstByItemIdAndStartAfterAndStatusNotOrderByStartAsc(
                        dto.getId(), now, BookingStatus.REJECTED);

        if (last != null) {
            dto.setLastBooking(new ItemDto.BookingShortDto(last.getId(), last.getBooker().getId()));
        }
        if (next != null) {
            dto.setNextBooking(new ItemDto.BookingShortDto(next.getId(), next.getBooker().getId()));
        }

        return dto;
    }
}
