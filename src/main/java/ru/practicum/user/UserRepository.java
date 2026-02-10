package ru.practicum.user;

import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();

    Optional<User> findById(Long id);

    User save(User user);

    void delete(Long id);

    boolean existsByEmail(String email);

    boolean existsById(Long id);
}

