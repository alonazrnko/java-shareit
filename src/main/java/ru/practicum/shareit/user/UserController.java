package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> findAll() {
        log.info("Received request to find all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Long id) {
        log.info("Received request to find user with id: {}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("Received request to create user: {}", userDto);
        return userService.createUser(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto userDto) {
        log.info("Received request to update user with id: {}", id);
        return userService.updateUser(id, userDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Received request to delete user with id: {}", id);
        userService.deleteUser(id);
    }
}