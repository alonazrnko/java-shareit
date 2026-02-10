package ru.practicum.user;

import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

interface UserRepository {
    List<User> findAll();

    Optional<User> findById(Long id);

    User save(User user);

    void delete(Long id);

    boolean existsByEmail(String email);
}

