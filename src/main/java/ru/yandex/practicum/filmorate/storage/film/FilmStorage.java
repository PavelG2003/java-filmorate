package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film newFilm);

    Collection<Film> getFilms();

    Optional<Film> getFilmById(Long id);

    Collection<Film> getSortedFilmList(int count);
}
