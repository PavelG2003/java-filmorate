package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserStorage userStorage;

    @GetMapping
    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody @Valid User user) {
        return userStorage.create(user);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User update(@RequestBody User newUser) {
       return userStorage.update(newUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userStorage.delete(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void addFriends(@PathVariable Long id, @PathVariable Long friendId) {
        if ((id == null) || (friendId == null)) {
            throw new ValidationException("Id пользователя не может быть равно null");
        }
        userService.addFriends(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        if ((id == null) || (friendId == null)) {
            throw new ValidationException("Id пользователя не может быть равно null");
        }
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Set<Long> getUserFriends(@PathVariable Long id) {
        if (id == null) {
            throw new ValidationException("Id пользователя не может быть равно null");
        }
        return userService.getUserFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<Long> getCombinedFriends(@PathVariable Long id, @PathVariable Long otherId) {
        if ((id == null) || (otherId == null)) {
            throw new ValidationException("Id пользователя не может быть равно null");
        }
        return userService.getCombinedFriends(id, otherId);
    }

}
