package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    public User create(User film);

    public User update(User newFilm);

    public Collection<User> getUsers();

    public Optional<User> getUserById(Long userId);

    public void delete(Long userId);
}
