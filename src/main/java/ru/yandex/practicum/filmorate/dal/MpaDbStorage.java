package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

@Repository
public class MpaDbStorage extends BaseRepository<Mpa> {
    private static final String FIND_ALL_MPA_QUERY = "SELECT * FROM mpa";
    private static final String FIND_MPA_BY_ID_QUERY = "SELECT * FROM mpa WHERE id = ?";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Mpa> getMpaCollection() {
        return findMany(FIND_ALL_MPA_QUERY);
    }

    public Optional<Mpa> getMpaById(Long mpaId) {
        return findOne(FIND_MPA_BY_ID_QUERY, mpaId);
    }
}
