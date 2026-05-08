package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void search_WhenTextMatchesNameOrDescription_ShouldReturnItems() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        userRepository.save(owner);

        Item item1 = new Item();
        item1.setName("Аккумуляторная дрель");
        item1.setDescription("Отличная дрель для ремонта");
        item1.setAvailable(true);
        item1.setOwner(owner);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Отвертка");
        item2.setDescription("Крестовая, отлично крутит саморезы");
        item2.setAvailable(true);
        item2.setOwner(owner);
        itemRepository.save(item2);

        Item item3 = new Item();
        item3.setName("Старая дрель");
        item3.setDescription("Сломана");
        item3.setAvailable(false);
        item3.setOwner(owner);
        itemRepository.save(item3);

        List<Item> result = itemRepository.search("ДрЕлЬ", PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Аккумуляторная дрель");
    }
}
