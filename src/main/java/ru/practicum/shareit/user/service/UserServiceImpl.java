package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = userMapper.toUser(userDto);
        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }

        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}