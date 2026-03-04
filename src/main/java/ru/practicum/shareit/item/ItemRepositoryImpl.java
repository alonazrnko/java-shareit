package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idGenerator.getAndIncrement());
            log.info("Generated new ID: {} for item: {}", item.getId(), item.getName());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String query = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable) // Только доступные вещи
                .filter(item -> item.getName().toLowerCase().contains(query)
                        || item.getDescription().toLowerCase().contains(query))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        items.remove(id);
    }
}
