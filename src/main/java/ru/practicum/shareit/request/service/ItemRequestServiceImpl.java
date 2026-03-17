package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestResponseDto createRequest(Long userId, ItemRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestRepository.save(request);
        return mapToResponseDto(savedRequest, Collections.emptyList());
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        checkUserExists(userId);
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        return enrichRequestsWithItems(requests);
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId) {
        checkUserExists(userId);
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNot(
                userId, Sort.by(Sort.Direction.DESC, "created")
        );
        return enrichRequestsWithItems(requests);
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        checkUserExists(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        List<Item> items = itemRepository.findAllByRequestIdIn(List.of(requestId));
        return mapToResponseDto(request, items);
    }


    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
    }

    private List<ItemRequestResponseDto> enrichRequestsWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();

        Map<Long, List<Item>> itemsByRequestId = itemRepository.findAllByRequestIdIn(requestIds)
                .stream()
                .collect(Collectors.groupingBy(Item::getRequestId));

        return requests.stream()
                .map(req -> mapToResponseDto(req, itemsByRequestId.getOrDefault(req.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private ItemRequestResponseDto mapToResponseDto(ItemRequest request, List<Item> items) {
        List<ItemRequestResponseDto.ItemForRequestDto> itemDtos = items.stream()
                .map(item -> ItemRequestResponseDto.ItemForRequestDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .available(item.getAvailable())
                        .requestId(item.getRequestId())
                        .ownerId(item.getOwner().getId())
                        .build())
                .collect(Collectors.toList());

        return ItemRequestResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemDtos)
                .build();
    }
}
