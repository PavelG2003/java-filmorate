package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet result, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(result.getLong("id"));
        film.setName(result.getString("name"));
        film.setDescription(result.getString("description"));
        Date releaseDate = result.getDate("release_date");
        film.setReleaseDate(releaseDate.toLocalDate());
        film.setDuration(result.getInt("duration"));
        return film;
    }
}
