package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {
    public static Film mapToFilm(NewFilmRequest filmRequest) {
        Film film  = new Film();
        film.setName(filmRequest.getName());
        film.setDescription(filmRequest.getDescription());
        film.setReleaseDate(filmRequest.getReleaseDate());
        film.setDuration(filmRequest.getDuration());
        return film;
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest filmRequest) {
        if (filmRequest.hasName()) {
            film.setName(filmRequest.getName());
        }
        if (filmRequest.hasDescription()) {
            film.setDescription(filmRequest.getDescription());
        }
        if (filmRequest.hasReleaseDate()) {
            film.setReleaseDate(filmRequest.getReleaseDate());
        }
        if (filmRequest.hasDuration()) {
            film.setDuration(filmRequest.getDuration());
        }
        return film;
    }
}
