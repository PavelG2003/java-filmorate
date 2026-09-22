package ru.yandex.practicum.filmorate.storage.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;

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
        return users.values();
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    @Override
    public User create(@RequestBody @Valid User user) {
        user.setId(getNextId());
        log.debug("Пользователю сгенерирован id: {}", user.getId());
        users.put(user.getId(), user);
        log.info("Пользователь с id {} добавлен во внутреннее хранилеще", user.getId());
        return user;
    }

    @Override
    public User update(@RequestBody User oldUser) {
        users.put(oldUser.getId(), oldUser);
        log.info("Пользователь с id: {} успешно обновлён", oldUser.getId());
        return oldUser;
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    private long getNextId() {
        long curMaxId = users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++curMaxId;
    }
}
