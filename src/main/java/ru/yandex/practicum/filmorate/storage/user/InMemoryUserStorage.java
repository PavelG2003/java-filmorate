package ru.yandex.practicum.filmorate.storage.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getUsers() {
        log.info("GET /users - запрос всех пользователей");
        return users.values();
    }

    @Override
    public void delete(Long id) {
        User user = getUserById(id)
                .orElseThrow(() -> new NotFoundException("Указанный пользователь c id: " + id + " не найден"));
        users.remove(user.getId());
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public User create(@RequestBody @Valid User user) {
        log.info("Post /users создание нового пользователя: {}", user.getName());
        if ((user.getName() == null) || (user.getName().isBlank())) {
            log.debug("Имя пользователя заменено на логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        log.debug("Пользователю сгенерирован id: {}", user.getId());
        users.put(user.getId(), user);
        log.info("Пользователь с id {} добавлен во внутреннее хранилеще", user.getId());
        return user;
    }

    @Override
    public User update(@RequestBody User newUser) {
        log.info("PUT /users - обновление пользователя с id: {}", newUser.getId());
        Long newUserId = newUser.getId();
        if (newUserId == null) {
            log.warn("При изменении пользователя не передали id");
            throw new ConditionsNotMetException("id должен быть указан");
        }
        if (users.containsKey(newUserId)) {
            User oldUser = users.get(newUserId);
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


            log.info("Пользователь с id: {} успешно обновлён", newUserId);
            return oldUser;
        } else {
            log.warn("Пользователь с id: {} не был найден", newUserId);
            throw new NotFoundException("Пользователь с id: " + newUserId + "не был найден");
        }
    }

    private long getNextId() {
        long curMaxId = users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++curMaxId;
    }
}
