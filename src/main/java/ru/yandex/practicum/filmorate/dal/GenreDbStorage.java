package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

@Repository
public class GenreDbStorage extends BaseRepository<Genre> {
    private static final String FIND_ALL_GENRES_QUERY = "SELECT * FROM genres";
    private static final String FIND_GENRE_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";

   public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
       super(jdbc, mapper);
   }

   public Collection<Genre> getGenres() {
       return findMany(FIND_ALL_GENRES_QUERY);
   }

   public Optional<Genre> getGenreById(Long id) {
       return findOne(FIND_GENRE_BY_ID_QUERY, id);
   }

}
