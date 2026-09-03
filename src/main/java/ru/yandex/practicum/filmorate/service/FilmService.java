package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final LocalDate CINEMA_BIRTH_DATE = LocalDate.of(1895, 12, 28);

    public Collection<Film> getFilms() {
        log.info("GET /films - запрос всех фильмов");
        return filmStorage.getFilms();
    }

    public Film create(@RequestBody @Valid Film film) {
        log.info("POST /films, создание нового фильма: {}", film.getName());
        if (film.getReleaseDate().isBefore(CINEMA_BIRTH_DATE)) {
            log.warn("Ошибка валидации даты релиза фильма, получен: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше " + CINEMA_BIRTH_DATE);
        }
        return filmStorage.create(film);
    }

    public Film update(@RequestBody Film newFilm) {
        log.info("PUT /films - обновление пользователя с id: {}", newFilm.getId());
        Long newFilmId = newFilm.getId();
        if (newFilmId == null) {
            log.warn("При изменении фильма не передали id");
            throw new ConditionsNotMetException("id должен быть указан");
        }
        Film oldFilm = getFilmById(newFilmId);
        if (newFilm.getName() != null) {
            if (newFilm.getName().isBlank()) {
                log.warn("Ошибка валидации имени фильма, получен {}", newFilm.getName());
                throw new ConditionsNotMetException("Название не может быть пустым");
            }
            oldFilm.setName(newFilm.getName());
        }
        if  (newFilm.getDescription() != null) {
            if (newFilm.getDescription().length() > 200) {
                log.warn("Ошибка валидации описания фильма, получен: {}", newFilm.getDescription());
                throw new ConditionsNotMetException("Описание фильма не может быть больше 200 символов");
            }
            oldFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getDuration() <= 0) {
            log.warn("Ошибка валидации продолжительности фильма, получено: {}", newFilm.getDuration());
            throw new ConditionsNotMetException("Продолжительность фильма не может быть отрицательным числом");
        }
        oldFilm.setDuration(newFilm.getDuration());

        if (newFilm.getReleaseDate() != null) {
            if (newFilm.getReleaseDate().isBefore(CINEMA_BIRTH_DATE)) {
                log.warn("Ошибка валидации даты релиза фильма, получено: {}", newFilm.getReleaseDate());
                throw new ValidationException("Дата релиза фильма не может быть раньше " + CINEMA_BIRTH_DATE);
            }
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        return filmStorage.update(oldFilm);
    }

    public void addLike(Long id, Long userId) {
        log.info("Попытка добавить лайк фильму с id: {} от пользователя с id: {}", id, userId);
        Film film = getFilmById(id);
        User user = getUserById(userId);
        Set<User> filmLikes = film.getLikes();
        if (filmLikes != null && filmLikes.contains(user)) {
            log.warn("Лайк уже существует: фильм id={}, пользователь id={}", id, userId);
            throw new DuplicatedDataException(
                    "У фильма с id: " + id + " уже стоит лайк от пользователя с id: " + userId
            );
        }
        filmLikes.add(user);
        log.info("Лайк успешно добавлен: фильм id={}, пользователь id={}, текущее количество лайков: {}",
                id, userId, filmLikes.size());
    }

    public void deleteLike(Long id, Long userId) {
        log.info("Попытка удалить лайк у фильма с id: {} от пользователя с id: {}", id, userId);
        Film film = getFilmById(id);
        User user = getUserById(userId);
        Set<User> filmLikes = film.getLikes();
        if (filmLikes != null && !filmLikes.contains(user)) {
            log.warn("Лайк не найден: фильм id={}, пользователь id={}", id, userId);
            throw new NotFoundException(
                    "У фильма с id: " + id + " не стоит лайк от пользователя с id: " + userId
            );
        }
        filmLikes.remove(user);
        log.info("Лайк успешно удален: фильм id={}, пользователь id={}, текущее количество лайков: {}",
                id, userId, filmLikes != null ? filmLikes.size() : 0);
    }

    public Collection<Film> getFilmList(String count) {
        log.info("Попытка вывести первые {} фильмов отсортированных по убыванию лайкров", count);
        int intCount;
        try {
            intCount = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            log.warn("Ошибка преобразования строки {} в число", count);
            throw new ValidationException("строка " + count + " не соответствует ожидаемому числовому формату");
        }
        if (intCount <= 0) {
            log.warn("Некорректное значение count: {}, (должно быть проложительным числом)", count);
            throw new ValidationException("count не может быть отрицательным числом или нулём");
        }
        log.info("Успешно получен список отсортированных по лайкам фильмов");
        return filmStorage.getSortedFilmList(intCount);
    }

    private Film getFilmById(long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id: " + id + " не найден"));
    }

    private User getUserById(long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + id + " не найден"));
    }
}
