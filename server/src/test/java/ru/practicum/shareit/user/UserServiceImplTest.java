package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test Name");
        user.setEmail("test@mail.com");

        // Initialize expected DTO
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test Name");
        userDto.setEmail("test@mail.com");
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(userMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());

        verify(userMapper, times(1)).toUserDto(user);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(999L));

        verify(userMapper, never()).toUserDto(any());
    }

    @Test
    void deleteUser_ShouldInvokeRepositoryDelete() {
        userService.delete(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void update_WhenOnlyNameChanged_ShouldUpdateNameOnly() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old");
        existingUser.setEmail("old@mail.com");

        ru.practicum.shareit.user.dto.UserDto patchDto = new ru.practicum.shareit.user.dto.UserDto();
        patchDto.setName("New");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ru.practicum.shareit.user.dto.UserDto resultDto = new ru.practicum.shareit.user.dto.UserDto();
        resultDto.setId(1L);
        resultDto.setName("New");
        resultDto.setEmail("old@mail.com");
        when(userMapper.toUserDto(any(User.class))).thenReturn(resultDto);

        ru.practicum.shareit.user.dto.UserDto result = userService.update(1L, patchDto);

        assertEquals("New", result.getName());
        assertEquals("old@mail.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getAll_ShouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toUserDto(any(User.class))).thenReturn(new ru.practicum.shareit.user.dto.UserDto());

        List<ru.practicum.shareit.user.dto.UserDto> result = userService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void delete_ShouldInvokeRepositoryDelete() {
        userService.delete(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}