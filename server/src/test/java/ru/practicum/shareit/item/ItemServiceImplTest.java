package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
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
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private Item item;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Commenter");

        item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setOwner(user);

        commentDto = new CommentDto();
        commentDto.setText("Great drill!");
    }

    @Test
    void addComment_WhenUserNeverBookedItem_ShouldThrowException() {
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                anyLong(),
                anyLong(),
                any(LocalDateTime.class),
                any()
        )).thenReturn(false);

        assertThrows(BadRequestException.class, () -> itemService.addComment(1L, 1L, commentDto));

        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_WhenValid_ShouldSaveComment() {
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                anyLong(),
                anyLong(),
                any(LocalDateTime.class),
                any()
        )).thenReturn(true);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ru.practicum.shareit.item.model.Comment savedComment = new ru.practicum.shareit.item.model.Comment();
        savedComment.setId(1L);
        savedComment.setText(commentDto.getText());
        savedComment.setAuthor(user);
        savedComment.setItem(item);
        savedComment.setCreated(LocalDateTime.now());

        when(commentRepository.save(any(ru.practicum.shareit.item.model.Comment.class))).thenReturn(savedComment);
        when(itemMapper.toCommentDto(any())).thenReturn(commentDto);

        CommentDto result = itemService.addComment(1L, 1L, commentDto);

        assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(commentDto.getText(), result.getText());

        verify(commentRepository, times(1)).save(any(ru.practicum.shareit.item.model.Comment.class));
    }

    @Test
    void update_WhenUserIsNotOwner_ShouldThrowNotFoundException() {
        User owner = new User();
        owner.setId(2L);
        owner.setName("Owner");

        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setOwner(owner);

        ItemDto patchDto = new ItemDto();
        patchDto.setName("New Name");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));

        assertThrows(ru.practicum.shareit.exception.NotFoundException.class,
                () -> itemService.update(1L, 1L, patchDto));

        verify(itemRepository, never()).save(any());
    }

    @Test
    void search_WhenTextIsBlank_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.search("", 0, 10);

        assertTrue(result.isEmpty());
        verify(itemRepository, never()).search(anyString(), any());
    }

    @Test
    void getById_WhenValid_ShouldReturnItemWithBookings() {
        User owner = new User();
        owner.setId(2L);
        item.setOwner(owner);

        ru.practicum.shareit.item.dto.ItemDto mockDto = new ru.practicum.shareit.item.dto.ItemDto();
        mockDto.setId(1L);
        mockDto.setName("Drill");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(1L)).thenReturn(List.of());

        when(itemMapper.toItemDto(any(Item.class))).thenReturn(mockDto);

        var result = itemService.getItemById(1L, 2L);

        assertNotNull(result);
        assertEquals("Drill", result.getName());
        verify(itemMapper).toItemDto(any(Item.class));
    }}
