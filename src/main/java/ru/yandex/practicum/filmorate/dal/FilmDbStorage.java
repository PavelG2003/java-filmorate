package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

@Repository
public class FilmDbStorage extends BaseRepository<Film> {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?," +
            " releaseDate = ?, duration = ? WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration) " +
            "VALUES (?, ?, ?, ?)";
    private static final String ADD_LIKE_QUERY = "INSERT INTO film_likes(film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_id = ?, user_id = ?";
    private static final String FIND_POPULAR_QUERY = "SELECT f.*" +
            "        FROM films AS f" +
            "        LEFT JOIN film_likes AS fl ON fl.film_id = f.id" +
            "        GROUP BY" +
            "            f.id," +
            "            f.name," +
            "            f.description," +
            "            f.release_date," +
            "            f.duration" +
            "        ORDER BY COUNT(fl.user_id) DESC, f.id" +
            "        LIMIT ?";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    public Optional<Film> getFilmById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public List<Film> getFilms() {
        return findMany(FIND_ALL_QUERY);
    }

    public Film create(Film film) {
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration()
                );
        film.setId(id);
        return film;
    }

    public Film update(Film film) {
        update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId()
                );
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
        return findMany(FIND_POPULAR_QUERY, count);
    }
}
