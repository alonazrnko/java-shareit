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
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private ItemMapper commentMapper;

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
        when(commentMapper.toCommentDto(any())).thenReturn(commentDto);

        CommentDto result = itemService.addComment(1L, 1L, commentDto);

        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(commentDto.getText(), result.getText());

        verify(commentRepository, times(1)).save(any(ru.practicum.shareit.item.model.Comment.class));
    }
}
