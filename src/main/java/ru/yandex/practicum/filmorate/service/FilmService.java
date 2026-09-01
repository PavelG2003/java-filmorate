package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(Long id, Long userId) {
        Film film = filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id: " + id + " не найден"));
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Указанный пользователь c id: " + userId + " не найден"
                ));
        Set<User> filmLikes = film.getLikes();
        if (filmLikes != null && filmLikes.contains(user)) {
            throw new DuplicatedDataException(
                    "У фильма с id: " + id + " уже стоит лайк от пользователя с id: " + userId
            );
        }
        filmLikes.add(user);
    }

    public void deleteLike(Long id, Long userId) {
        Film film = filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id: " + id + " не найден"));
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Указанный пользователь c id: " + userId + " не найден"
                ));
        Set<User> filmLikes = film.getLikes();
        if (filmLikes != null && !filmLikes.contains(user)) {
            throw new NotFoundException(
                    "У фильма с id: " + id + " не стоит лайк от пользователя с id: " + userId
            );
        }
        filmLikes.remove(user);
    }

    public Collection<Film> getFilmList(int count) {
        Collection<Film> films = filmStorage.getFilms();
        if (films.isEmpty()) {
            return Collections.emptyList();
        }
        return films.stream()
                .sorted((f1, f2) -> Integer.compare(
                        f2.getLikes().size(),
                        f1.getLikes().size()
                ))
                .limit(count)
                .collect(Collectors.toList());
    }
}
