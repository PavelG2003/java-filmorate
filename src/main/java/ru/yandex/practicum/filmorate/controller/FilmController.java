package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate CINEMA_BIRTH_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> getFilms() {
        log.info("GET /films - запрос всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        log.info("POST /films, создание нового фильма: {}", film.getName());

        if (film.getReleaseDate().isBefore(CINEMA_BIRTH_DATE)) {
            log.warn("Ошибка валидации даты релиза фильма, получен: {}", film.getReleaseDate());
            throw new ConditionsNotMetException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        film.setId(getNextId());
        log.debug("Фильму сгенерирован id: {}", film.getId());
        films.put(film.getId(), film);
        log.info("Фильм с id {} добавлен во внутреннее хранилеще", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("PUT /films - обновление пользователя с id: {}", newFilm.getId());
        Long newFilmId = newFilm.getId();
        if (newFilmId == null) {
            log.warn("При изменении фильма не передали id");
            throw new ConditionsNotMetException("id должен быть указан");
        }

        if (films.containsKey(newFilmId)) {
            Film oldFilm = films.get(newFilmId);
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
                    throw new ConditionsNotMetException("Дата релиза фильма не может быть раньше " + CINEMA_BIRTH_DATE);
                }
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }


            log.info("Фильм с id: {} успешно обновлён", newFilmId);
            return oldFilm;
        }
        log.warn("Фильм с id: {} не был найден", newFilmId);
        throw new NotFoundException("Фильм с id: " + newFilmId + " не найден");
    }

    private long getNextId() {
        long maxFilmId = films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++maxFilmId;
    }
}
