package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

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
    private final UserDbStorage userDbStorage;

    public Collection<User> getUsers() {
        log.info("GET /users - запрос всех пользователей");
        return userDbStorage.getUsers();
    }

    public User create(NewUserRequest request) {
        log.info("Post /users создание нового пользователя: {}", request.getName());
        if ((request.getName() == null) || (request.getName().isBlank())) {
            log.debug("Имя пользователя заменено на логин: {}", request.getLogin());
            request.setName(request.getLogin());
        }
        User user = UserMapper.mapToUser(request);
        return userDbStorage.create(user);
    }

    public User update(UpdateUserRequest request) {
        if (request.getId() == null) {
            log.warn("При изменении пользователя не передали id");
            throw new ConditionsNotMetException("id должен быть указан");
        }
        long requestId = request.getId();
        log.info("PUT /users - обновление пользователя с id: {}", requestId);

        User updatedUser = userDbStorage.getUserById(requestId)
                .map(user -> UserMapper.updateUserFields(user, request))
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + requestId + " не найден"));
        return userDbStorage.update(updatedUser);
    }

    public void deleteUser(@PathVariable Long userId) {
        findUserInDb(userId);
        userDbStorage.deleteUser(userId);
    }

    public void addFriends(Long userId, Long friendId) {
        log.info("Попытка добавить в друзья пользователя с id: {} к пользователю с id: {}", friendId, userId);

        validateIdentialIds(userId, friendId);
        log.debug("Проверка на дублирование id пройдена успешно");

        User user = findUserInDb(userId);
        User friend = findUserInDb(friendId);
        log.debug("Пользователи найдены: {} и {}", user.getId(), friend.getId());

        Set<User> userFriends = getUserFriends(userId);
        if (userFriends != null && userFriends.contains(friend)) {
            log.warn("Пользователь {} уже есть в друзьях у пользователя {}", friendId, userId);
            throw new DuplicatedDataException(
                    "Пользователь с id: " + friendId + " уже есть в друзьях пользователя с id: " + userId
            );
        }
        userDbStorage.addFriend(userId, friendId);
        log.info("Дружба успешно установлена: пользователь {} и пользователь {} теперь друзья", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.info("Попытка удалить пользователя с id: {} из друзей пользователя с id: {}", friendId, userId);

        validateIdentialIds(userId, friendId);
        log.debug("Проверка на дублирование id пройдена успешно");

        findUserInDb(userId);
        findUserInDb(friendId);

        if (!userDbStorage.deleteFriend(userId, friendId)) {
            log.warn("Пользователь {} не найден в друзьях у пользователя {}, операция пропущена", friendId, userId);
           return;
        }
        log.info("Дружба успешно удалена: пользователь {} и пользователь {} больше не друзья", userId, friendId);
    }

    public Set<User> getCombinedFriends(Long userId, Long otherId) {
        log.info("Поиск общих друзей между пользователями с id: {} и {}", userId, otherId);

        validateIdentialIds(userId, otherId);
        log.debug("Проверка на дублирование id пройдена успешно");
        User user = findUserInDb(userId);
        log.debug("Пользователь найден: id={}", userId);

        User otherUser = findUserInDb(otherId);
        log.debug("Другой пользователь найден: id={}", otherId);

        Set<User> combinedFriends = userDbStorage.getCommonFriends(userId, otherId);
        log.info("Найдено {} общих друзей между пользователями {} и {}", combinedFriends.size(), userId, otherId);
        return combinedFriends;
    }

    public Set<User> getUserFriends(Long userId) {
       findUserInDb(userId);
       return userDbStorage.getUserFriends(userId);
    }

    private void validateIdentialIds(long id1, long id2) {
        if (Objects.equals(id1, id2)) {
            throw new ValidationException("Указан id одного и того же пользователя");
        }
    }

    private User findUserInDb(long userId) {
        return userDbStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }
}
