package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private static final LocalDate CINEMA_BIRTH_DATE = LocalDate.of(1895, 12, 28);

    public Collection<Film> getFilms() {
        log.info("GET /films - запрос всех фильмов");
        return filmDbStorage.getFilms();
    }

    public Film create(NewFilmRequest filmRequest) {
        log.info("POST /films, создание нового фильма: {}", filmRequest.getName());

        Film film = FilmMapper.mapToFilm(filmRequest);
        if (film.getReleaseDate().isBefore(CINEMA_BIRTH_DATE)) {
            log.warn("Ошибка валидации даты релиза фильма, получен: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше " + CINEMA_BIRTH_DATE);
        }
        return filmDbStorage.create(film);
    }

    public Film update(Long filmId, UpdateFilmRequest filmRequest) {
        log.info("PUT /films - обновление фильма с id: {}", filmId);

        if (filmId == null) {
            log.warn("При изменении фильма не передали id");
            throw new ConditionsNotMetException("id должен быть указан");
        }
        Film updatedFilm = filmDbStorage.getFilmById(filmId)
                .map(film -> FilmMapper.updateFilmFields(film, filmRequest))
                .orElseThrow(() -> new NotFoundException("film с id "  + filmId + "не найден"));
        return filmDbStorage.update(updatedFilm);
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Попытка добавить лайк фильму с id: {} от пользователя с id: {}", filmId, userId);
        if (filmId == null || userId == null) {
            log.warn("При высставлении лайка фильму не передали id фильму или пользователю");
            throw new ConditionsNotMetException("id должен быть указан");
        }

        try {
            filmDbStorage.addLike(filmId, userId);
        } catch (DuplicateKeyException e) {
            log.warn("Лайк уже существует: фильм id={}, пользователь id={}", filmId, userId);
            throw new DuplicateKeyException(
                    "У фильма с id: " + filmId + " уже стоит лайк от пользователя с id: " + userId
            );
        }
        log.info("Лайк успешно добавлен: фильм id={}, пользователь id={}",
                filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        log.info("Попытка удалить лайк у фильма с id: {} от пользователя с id: {}", filmId, userId);

        if (!filmDbStorage.deleteLike(filmId, userId)) {
            log.warn("Лайк не найден: фильм id={}, пользователь id={}", filmId, userId);
            throw new NotFoundException(
                    "У фильма с id: " + filmId + " не стоит лайк от пользователя с id: " + userId
            );
        }
        log.info("Лайк успешно удален: фильм id={}, пользователь id={}",
                filmId, userId);
    }

    public Collection<Film> getPopular(String count) {
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
        List<Film> popularList = filmDbStorage.getPopular(intCount);
        log.info("Успешно получен список отсортированных по лайкам фильмов");
        return  popularList;
    }
}
