package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create(User film);

    User update(User newFilm);

    Collection<User> getUsers();

    Optional<User> getUserById(Long userId);

    void delete(Long userId);
}
