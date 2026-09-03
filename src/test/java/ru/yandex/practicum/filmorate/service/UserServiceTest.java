package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());
    }

    @Test
    void createShouldUseLoginWhenNameIsBlank() {
        User user = createUser("mail@example.com", "login", "");

        User savedUser = userService.create(user);

        assertEquals("login", savedUser.getName());
        assertEquals(1L, savedUser.getId());
    }

    @Test
    void updateShouldChangeStoredUser() {
        User savedUser = userService.create(createUser("old@example.com", "old-login", "Старое имя"));
        User changes = createUser("new@example.com", "new-login", "Новое имя");
        changes.setId(savedUser.getId());

        User updatedUser = userService.update(changes);

        assertEquals("new@example.com", updatedUser.getEmail());
        assertEquals("new-login", updatedUser.getLogin());
        assertEquals("Новое имя", updatedUser.getName());
    }

    @Test
    void updateShouldRejectFutureBirthday() {
        User savedUser = userService.create(createUser("mail@example.com", "login", "Имя"));
        User changes = createUser("mail@example.com", "login", "Имя");
        changes.setId(savedUser.getId());
        changes.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ConditionsNotMetException.class, () -> userService.update(changes));
    }

    @Test
    void deleteShouldRejectUnknownUser() {
        assertThrows(NotFoundException.class, () -> userService.deleteUser(999L));
    }

    @Test
    void addAndDeleteFriendsShouldUpdateBothUsers() {
        User firstUser = userService.create(createUser("first@example.com", "first", "Первый"));
        User secondUser = userService.create(createUser("second@example.com", "second", "Второй"));

        userService.addFriends(firstUser.getId(), secondUser.getId());

        assertTrue(firstUser.getFriends().contains(secondUser));
        assertTrue(secondUser.getFriends().contains(firstUser));

        userService.deleteFriend(firstUser.getId(), secondUser.getId());

        assertFalse(firstUser.getFriends().contains(secondUser));
        assertFalse(secondUser.getFriends().contains(firstUser));
    }

    @Test
    void addFriendsShouldRejectSameUserId() {
        User user = userService.create(createUser("mail@example.com", "login", "Имя"));

        assertThrows(
                ValidationException.class,
                () -> userService.addFriends(user.getId(), user.getId())
        );
    }

    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}
