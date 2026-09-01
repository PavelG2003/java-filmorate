package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    public Film create(Film film);

    public Film update(Film newFilm);

    public Collection<Film> getFilms();

    public Optional<Film> getFilmById(Long id);
}
