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
        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = friend.getFriends();
        if (userFriends != null && userFriends.contains(friend.getId())) {
            throw new DuplicatedDataException("Пользователь с id: " + friendId + " уже есть в друзьях");
        }
        if (otherUserFriends != null && otherUserFriends.contains(user.getId())) {
            throw new DuplicatedDataException("Пользователь с id: " + friendId + " уже есть в друзьях");
        }
        userFriends.add(friend.getId());
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
        Set<Long> userFriends = user.getFriends();
        if (userFriends != null && !userFriends.contains(friend.getId())) {
            throw new ConditionsNotMetException("Пользователь с id: " + friendId + " уже есть в друзьях");
        }
        userFriends.remove(friend.getId());
    }

    public Set<Long> getCombinedFriends(Long userId, Long otherId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Указанный пользователь c id: " + userId + " не найден"
                ));
        User otherUser = userStorage.getUserById(otherId)
                .orElseThrow(() -> new NotFoundException(
                        "Указанный пользователь с id: " + otherId + " не найден"
                ));
        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = otherUser.getFriends();
        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .collect(Collectors.toSet());
    }

    public Set<Long> getUserFriends(Long userId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Указанный пользователь c id: " + userId + " не найден"
                ));
        return user.getFriends();
    }
}
