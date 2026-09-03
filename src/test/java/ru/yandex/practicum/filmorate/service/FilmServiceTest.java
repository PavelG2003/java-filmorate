package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmServiceTest {
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmService = new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage());
    }

    @Test
    void createShouldSaveValidFilm() {
        Film film = createFilm("Матрица", LocalDate.of(1999, 3, 31), 136);

        Film savedFilm = filmService.create(film);

        assertEquals(1L, savedFilm.getId());
        assertEquals(1, filmService.getFilms().size());
    }

    @Test
    void createShouldRejectReleaseDateBeforeCinemaBirthday() {
        Film film = createFilm("Слишком ранний фильм", LocalDate.of(1895, 12, 27), 1);

        assertThrows(ValidationException.class, () -> filmService.create(film));
        assertEquals(0, filmService.getFilms().size());
    }

    @Test
    void updateShouldChangeStoredFilm() {
        Film savedFilm = filmService.create(
                createFilm("Старое название", LocalDate.of(2000, 1, 1), 100)
        );
        Film changes = createFilm("Новое название", LocalDate.of(2001, 2, 2), 120);
        changes.setId(savedFilm.getId());

        Film updatedFilm = filmService.update(changes);

        assertEquals(savedFilm.getId(), updatedFilm.getId());
        assertEquals("Новое название", updatedFilm.getName());
        assertEquals(120, updatedFilm.getDuration());
    }

    @Test
    void updateShouldRequireId() {
        Film film = createFilm("Фильм", LocalDate.of(2000, 1, 1), 100);

        assertThrows(ConditionsNotMetException.class, () -> filmService.update(film));
    }

    @Test
    void updateShouldRejectUnknownFilm() {
        Film film = createFilm("Фильм", LocalDate.of(2000, 1, 1), 100);
        film.setId(999L);

        assertThrows(NotFoundException.class, () -> filmService.update(film));
    }

    @Test
    void getFilmListShouldRejectNonNumericCount() {
        assertThrows(ValidationException.class, () -> filmService.getFilmList("ten"));
    }

    @Test
    void getFilmListShouldReturnRequestedNumberOfFilms() {
        filmService.create(createFilm("Первый", LocalDate.of(2000, 1, 1), 100));
        filmService.create(createFilm("Второй", LocalDate.of(2001, 1, 1), 110));

        Collection<Film> films = filmService.getFilmList("1");

        assertEquals(1, films.size());
    }

    private Film createFilm(String name, LocalDate releaseDate, int duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        return film;
    }
}
