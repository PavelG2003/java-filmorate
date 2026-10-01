package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRowMapper implements RowMapper<Mpa> {
    @Override
    public Mpa mapRow(ResultSet result, int rowNum) throws SQLException {
        Mpa mpa = new Mpa();
        mpa.setId(result.getLong("id"));
        mpa.setName(result.getString("name"));
        return mpa;
    }
}
