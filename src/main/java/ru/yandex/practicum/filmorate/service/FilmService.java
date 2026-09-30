package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaDbStorage;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmDbStorage filmDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final MpaDbStorage mpaDbStorage;
    private static final LocalDate CINEMA_BIRTH_DATE = LocalDate.of(1895, 12, 28);

    public Film getFilmById(long id) {
        return filmDbStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

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
        if ((filmRequest.getMpa() == null) || (filmRequest.getMpa().getId() == null)) {
            throw new ValidationException("нужно указать id рейтинга mpa");
        }
        film.setMpa(getMpaById(filmRequest.getMpa().getId()));
        film.setGenres(resolveGenres(filmRequest.getGenres()));
        return filmDbStorage.create(film);
    }

    public Film update(UpdateFilmRequest filmRequest) {
        if (filmRequest.getId() == null) {
            log.warn("При изменении фильма не передали id");
            throw new ConditionsNotMetException("id должен быть указан");
        }
        long filmRequestId = filmRequest.getId();
        log.info("PUT /films - обновление фильма с id: {}", filmRequestId);

        Film updatedFilm = filmDbStorage.getFilmById(filmRequestId)
                .map(film -> FilmMapper.updateFilmFields(film, filmRequest))
                .orElseThrow(() -> new NotFoundException("фильм с id "  + filmRequestId + "не найден"));
        if (updatedFilm.getReleaseDate().isBefore(CINEMA_BIRTH_DATE)) {
            throw new ValidationException("Дата релиза — не раньше " + CINEMA_BIRTH_DATE);
        }
        if (updatedFilm.getMpa() == null || updatedFilm.getMpa().getId() == null) {
            throw new ValidationException("Нужно указать id рейтинга MPA");
        }
        updatedFilm.setMpa(getMpaById(updatedFilm.getMpa().getId()));
        updatedFilm.setGenres(resolveGenres(updatedFilm.getGenres()));
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

    public Collection<Genre> getGenres() {
        return genreDbStorage.getGenres();
    }

    public Genre getGenreById(Long genreId) {
        if (genreId == null) {
            log.warn("При запросе жанров не передали id");
            throw new ConditionsNotMetException("id должен быть указан");
        }
        return genreDbStorage.getGenreById(genreId)
                .orElseThrow(() -> new NotFoundException("жанр с id "  + genreId + "не найден"));
    }

    public Collection<Mpa> getMpaCollection() {
        return mpaDbStorage.getMpaCollection();
    }

    public Mpa getMpaById(Long mpaId) {
        if (mpaId == null) {
            log.warn("При запросе возрастных рейтингов не передали id");
            throw new ConditionsNotMetException("id должен быть указан");
        }
        return mpaDbStorage.getMpaById(mpaId)
                .orElseThrow(() -> new NotFoundException("возрастной рейтинг с id "  + mpaId + "не найден"));
    }

    private Set<Genre> resolveGenres(Set<Genre> requestGenres) {
        if (requestGenres == null || requestGenres.isEmpty()) {
            return new LinkedHashSet<>();
        }

        Set<Long> ids = new TreeSet<>();

        for (Genre genre : requestGenres) {
            if (genre == null || genre.getId() == null) {
                throw new ValidationException("Нужно указать id жанра");
            }
            ids.add(genre.getId());
        }

        Set<Genre> genres = new LinkedHashSet<>();

        for (Long id : ids) {
            genres.add(getGenreById(id));
        }

        return genres;
    }
}
