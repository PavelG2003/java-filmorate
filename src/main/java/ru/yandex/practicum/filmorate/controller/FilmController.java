package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;


@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@RequestBody @Valid Film film) {
        return filmStorage.create(film);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film update(@RequestBody Film newFilm) {
        return filmStorage.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        if ((id == null) || (userId == null)) {
            throw new ValidationException("Id пользователя или фильма не может быть равно null");
        }
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        if ((id == null) || (userId == null)) {
            throw new ValidationException("Id пользователя или фильма не может быть равно null");
        }
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getFilmList(@RequestParam(defaultValue = "10") String count) {
        int intCount = Integer.parseInt(count);
        if (intCount <= 0) {
            throw new ValidationException("count не может быть отрицательным числом");
        }
        return filmService.getFilmList(intCount);
    }

}
