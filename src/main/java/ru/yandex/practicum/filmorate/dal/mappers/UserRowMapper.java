package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet result, int rowNum) throws SQLException {
        User user = new User();
        user.setId(result.getLong("id"));
        user.setName(result.getString("name"));
        user.setEmail(result.getString("email"));
        user.setLogin(result.getString("login"));
        Date birthday = result.getDate("birthday");
        user.setBirthday(birthday.toLocalDate());
        return user;
    }
}
