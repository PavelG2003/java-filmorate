package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {
    public static User mapToUser(NewUserRequest userRequest) {
        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setLogin(userRequest.getLogin());
        user.setName(userRequest.getName());
        user.setBirthday(userRequest.getBirthday());
        return user;
    }

    public static User updateUserFields(User user, UpdateUserRequest userRequest) {
        if (userRequest.hasEmail()) {
            user.setEmail(user.getEmail());
        }
        if (userRequest.hasLogin()) {
            user.setLogin(user.getLogin());
        }
        if (userRequest.hasName()) {
            user.setName(user.getName());
        }
        if (userRequest.hasBirthday()) {
            user.setBirthday(userRequest.getBirthday());
        }
        return user;
    }
}
