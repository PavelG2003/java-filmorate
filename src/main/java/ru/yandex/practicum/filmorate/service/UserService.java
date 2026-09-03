package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getUsers() {
        log.info("GET /users - запрос всех пользователей");
        return userStorage.getUsers();
    }

    public User create(@RequestBody @Valid User user) {
        log.info("Post /users создание нового пользователя: {}", user.getName());
        if ((user.getName() == null) || (user.getName().isBlank())) {
            log.debug("Имя пользователя заменено на логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(@RequestBody User newUser) {
        log.info("PUT /users - обновление пользователя с id: {}", newUser.getId());
        Long newUserId = newUser.getId();
        if (newUserId == null) {
            log.warn("При изменении пользователя не передали id");
            throw new ConditionsNotMetException("id должен быть указан");
        }
        User oldUser = getUserById(newUserId);
        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName());
        }
        if (newUser.getBirthday() != null) {
            if (newUser.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Ошибка валидации дня рождения пользователя, получено: {}", newUser.getBirthday());
                throw new ConditionsNotMetException("День рождение не может быть в будущем");
            }
            oldUser.setBirthday(newUser.getBirthday());
        }
        if (newUser.getEmail() != null) {
            if (newUser.getEmail().isBlank()) {
                log.warn("Ошибка валидации Email пользователя, получено: {}", newUser.getEmail());
                throw new ConditionsNotMetException("Email не должен быть пустым");
            }
            if (!newUser.getEmail().contains("@")) {
                log.warn("Ошибка валидации Email пользователя, получено: {}", newUser.getEmail());
                throw new ConditionsNotMetException("Email должен содержать символ @");
            }
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getLogin() != null) {
            if (newUser.getLogin().isBlank()) {
                log.warn("Ошибка валидации Login пользователя, получено: {}", newUser.getLogin());
                throw new ConditionsNotMetException("Login не должен быть пустым");
            }
            oldUser.setLogin(newUser.getLogin());
        }
        if (oldUser.getName() == null) {
            oldUser.setName(newUser.getLogin());
        }
        return userStorage.update(oldUser);
    }

    public void deleteUser(@PathVariable Long id) {
        User user = getUserById(id);
        userStorage.delete(user.getId());
    }

    public void addFriends(Long userId, Long friendId) {
        log.info("Попытка добавить в друзья пользователя с id: {} к пользователю с id: {}", friendId, userId);
        validateIdentialIds(userId, friendId);
        log.debug("Проверка на дублирование id пройдена успешно");
        User user = getUserById(userId);
        log.debug("Пользователь найден: id={}, email={}", user.getId(), user.getEmail());
        User friend = getUserById(friendId);
        log.debug("Друг найден: id={}, email={}", friend.getId(), friend.getEmail());
        Set<User> userFriends = user.getFriends();
        Set<User> otherUserFriends = friend.getFriends();
        log.debug("Текущее количество друзей у пользователя {}: {}", userId, userFriends.size());
        log.debug("Текущее количество друзей у пользователя {}: {}", friendId, otherUserFriends.size());
        if (userFriends != null && userFriends.contains(friend)) {
            log.warn("Пользователь {} уже есть в друзьях у пользователя {}", friendId, userId);
            throw new DuplicatedDataException(
                    "Пользователь с id: " + friendId + " уже есть в друзьях пользователя с id: " + userId
            );
        }
        userFriends.add(friend);
        otherUserFriends.add(user);
        log.info("Дружба успешно установлена: пользователь {} и пользователь {} теперь друзья", userId, friendId);
        log.info("У пользователя {} теперь {} друзей", userId, userFriends.size());
        log.info("У пользователя {} теперь {} друзей", friendId, otherUserFriends.size());
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.info("Попытка удалить пользователя с id: {} из друзей пользователя с id: {}", friendId, userId);

        validateIdentialIds(userId, friendId);
        log.debug("Проверка на дублирование id пройдена успешно");

        User user = getUserById(userId);
        log.debug("Пользователь найден: id={}", userId);

        User friend = getUserById(friendId);
        log.debug("Друг найден: id={}", friendId);

        Set<User> userFriends = user.getFriends();
        Set<User> otherUserFriends = friend.getFriends();
        if (userFriends == null || !userFriends.contains(friend)) {
            log.warn("Пользователь {} не найден в друзьях у пользователя {}, операция пропущена", friendId, userId);
           return;
        }

        userFriends.remove(friend);
        otherUserFriends.remove(user);

        log.info("Дружба успешно удалена: пользователь {} и пользователь {} больше не друзья", userId, friendId);
        log.info("У пользователя {} осталось {} друзей", userId, userFriends.size());
        log.info("У пользователя {} осталось {} друзей", friendId, otherUserFriends.size());
    }

    public Set<User> getCombinedFriends(Long userId, Long otherId) {
        log.info("Поиск общих друзей между пользователями с id: {} и {}", userId, otherId);

        validateIdentialIds(userId, otherId);
        log.debug("Проверка на дублирование id пройдена успешно");
        User user = getUserById(userId);
        log.debug("Пользователь найден: id={}", userId);

        User otherUser = getUserById(otherId);
        log.debug("Другой пользователь найден: id={}", otherId);

        Set<User> userFriends = user.getFriends();
        Set<User> otherUserFriends = otherUser.getFriends();
        if (userFriends == null || userFriends.isEmpty()) {
            log.info("У пользователя {} нет друзей, общих друзей не найдено", userId);
            return new HashSet<>();
        }

        if (otherUserFriends == null || otherUserFriends.isEmpty()) {
            log.info("У пользователя {} нет друзей, общих друзей не найдено", otherId);
            return new HashSet<>();
        }

        Set<User> combinedFriends = userFriends.stream()
                .filter(otherUserFriends::contains)
                .collect(Collectors.toSet());
        log.info("Найдено {} общих друзей между пользователями {} и {}", combinedFriends.size(), userId, otherId);
        return combinedFriends;
    }

    public Set<User> getUserFriends(Long userId) {
        User user = getUserById(userId);
        return user.getFriends();
    }

    private User getUserById(long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Указанный пользователь c id: " + id + " не найден"));
    }

    private void validateIdentialIds(long id1, long id2) {
        if (Objects.equals(id1, id2)) {
            throw new ValidationException("Указан id одного и того же пользователя");
        }
    }
}
