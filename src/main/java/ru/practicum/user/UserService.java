package ru.practicum.user;

import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;

import java.util.List;

interface UserService {
    List<UserDto> getAllUsers();

    UserDto getUserById(Long id);

    UserDto createUser(UserDto userDto);

    UserDto updateUser(Long id, UserDto userDto);

    void deleteUser(Long id);
}
