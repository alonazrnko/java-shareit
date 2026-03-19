package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void createItem_WhenAllValid_ShouldReturnOk() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Обычная дрель");
        itemDto.setAvailable(true);

        when(itemClient.createItem(anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        verify(itemClient, times(1)).createItem(eq(1L), any(ItemDto.class));
    }

    @Test
    void createItem_WhenNameIsBlank_ShouldReturnBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("");
        itemDto.setDescription("Обычная дрель");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createItem(anyLong(), any(ItemDto.class));
    }

    @Test
    void createItem_WhenNoUserIdHeader_ShouldReturnBadRequest() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Обычная дрель");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createItem(anyLong(), any(ItemDto.class));
    }
}