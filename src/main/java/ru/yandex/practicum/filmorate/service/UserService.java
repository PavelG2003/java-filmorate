package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public void addFriends(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Указанный пользователь c id: " + userId + " не найден"
                ));
        User friend = userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException(
                        "Указанный пользователь с id: " + friendId + " не найден"
                ));
        Set<User> userFriends = user.getFriends();
        Set<User> otherUserFriends = friend.getFriends();
        if (userFriends != null && userFriends.contains(friend)) {
            throw new DuplicatedDataException("Пользователь с id: " + friendId + " уже есть в друзьях");
        }
        if (otherUserFriends != null && otherUserFriends.contains(user)) {
            throw new DuplicatedDataException("Пользователь с id: " + friendId + " уже есть в друзьях");
        }
        userFriends.add(friend);
        otherUserFriends.add(user);
        user.getFriends();
    }

    public void deleteFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Указанный пользователь c id: " + userId + " не найден"
                ));
        User friend = userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException(
                        "Указанный пользователь с id: " + friendId + " не найден"
                ));
        Set<User> userFriends = user.getFriends();
        Set<User> otherUserFriends = friend.getFriends();
        if (userFriends == null || !userFriends.contains(friend)) {
           return;
        }
        if (otherUserFriends == null && !otherUserFriends.contains(user)) {
            return;
        }
        userFriends.remove(friend);
        otherUserFriends.remove(user);
    }

    public Set<User> getCombinedFriends(Long userId, Long otherId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Указанный пользователь c id: " + userId + " не найден"
                ));
        User otherUser = userStorage.getUserById(otherId)
                .orElseThrow(() -> new NotFoundException(
                        "Указанный пользователь с id: " + otherId + " не найден"
                ));
        Set<User> userFriends = user.getFriends();
        Set<User> otherUserFriends = otherUser.getFriends();
        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .collect(Collectors.toSet());
    }

    public Set<User> getUserFriends(Long userId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Указанный пользователь c id: " + userId + " не найден"
                ));
        return user.getFriends();
    }
}
