package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User requester;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Нужна дрель");
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequestor(requester);
    }

    @Test
    void getAllRequests_ShouldReturnPagedResult() {
        User viewer = new User();
        viewer.setId(2L);
        when(userRepository.existsById(anyLong())).thenReturn(true);

        when(itemRequestRepository.findAllByRequestorIdNot(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(itemRequest));

        when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(List.of());

        List<ru.practicum.shareit.request.dto.ItemRequestResponseDto> result =
                itemRequestService.getAllRequests(viewer.getId(), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals(itemRequest.getDescription(), result.get(0).getDescription());

        verify(itemRequestRepository, times(1)).findAllByRequestorIdNot(eq(viewer.getId()), any(Pageable.class));
    }
}