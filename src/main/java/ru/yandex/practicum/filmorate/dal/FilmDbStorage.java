package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;

@Repository
public class FilmDbStorage extends BaseRepository<Film> {
    private static final String SELECT_FILMS = "SELECT f.*, m.name AS mpa_name FROM films f " +
            "LEFT JOIN mpa m ON m.id = f.mpa_id ";
    private static final String FIND_BY_ID_QUERY = SELECT_FILMS + "WHERE f.id = ?";
    private static final String FIND_ALL_FILMS_QUERY = SELECT_FILMS + "ORDER BY f.id";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?," +
            " release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String ADD_LIKE_QUERY = "INSERT INTO film_likes(film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_POPULAR_QUERY = SELECT_FILMS +
            "ORDER BY (SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id) DESC, f.id LIMIT ?";
    private static final String LOAD_GENRES_QUERY = "SELECT g.* FROM genres g JOIN films_genres fg ON fg.genre_id = g.id" +
            " WHERE fg.film_id = ? ORDER BY g.id";
    private static final String SAVE_GENRES_QUERY_DELETE = "DELETE FROM films_genres WHERE film_id = ?";
    private static final String SAVE_GENRES_QUERY_INSERT = "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    public Optional<Film> getFilmById(long id) {
        return findOne(FIND_BY_ID_QUERY, id).map(this::loadGenres);
    }

    public List<Film> getFilms() {
        return findMany(FIND_ALL_FILMS_QUERY).stream().map(this::loadGenres).toList();
    }

    @Transactional
    public Film create(Film film) {
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
                );
        film.setId(id);
        saveGenres(film);
        return film;
    }

    @Transactional
    public Film update(Film film) {
        update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
                );
        saveGenres(film);
        return film;
    }

    public void addLike(long filmId, long userId) {
        jdbc.update(ADD_LIKE_QUERY, filmId, userId);
    }

    public boolean deleteLike(long filmId, long userId) {
        int deleteRows = jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
        return deleteRows > 0;
    }

    public List<Film> getPopular(int count) {
        return findMany(FIND_POPULAR_QUERY, count).stream().map(this::loadGenres).toList();
    }

    private Film loadGenres(Film film) {
        film.setGenres(new LinkedHashSet<>(jdbc.query(
                LOAD_GENRES_QUERY,
                new GenreRowMapper(), film.getId())));
        return film;
    }

    private void saveGenres(Film film) {
        jdbc.update(
                SAVE_GENRES_QUERY_DELETE,
                film.getId()
        );

        List<Object[]> batch = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .toList();
        jdbc.batchUpdate(SAVE_GENRES_QUERY_INSERT, batch);
    }
}
