package ru.practicum.item;

import ru.practicum.item.model.Item;
import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);

    Optional<Item> findById(Long id);

    List<Item> findByOwnerId(Long ownerId);

    List<Item> search(String text);

    void delete(Long id);
}
