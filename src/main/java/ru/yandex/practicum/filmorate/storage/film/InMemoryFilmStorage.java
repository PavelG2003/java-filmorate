package ru.yandex.practicum.filmorate.storage.film;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film create(@RequestBody @Valid Film film) {
        film.setId(getNextId());
        log.debug("Фильму сгенерирован id: {}", film.getId());
        films.put(film.getId(), film);
        log.info("Фильм с id {} добавлен во внутреннее хранилеще", film.getId());
        return film;
    }

    @Override
    public Film update(@RequestBody Film oldFilm) {
        films.put(oldFilm.getId(), oldFilm);
        log.info("Фильм с id: {} успешно обновлён", oldFilm.getId());
        return oldFilm;
    }

    @Override
    public Collection<Film> getSortedFilmList(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(
                        f2.getLikes().size(),
                        f1.getLikes().size()
                ))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    private long getNextId() {
        long maxFilmId = films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++maxFilmId;
    }
}
