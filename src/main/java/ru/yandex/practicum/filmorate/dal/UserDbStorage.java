package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDbStorage extends BaseRepository<User> {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM users ORDER BY id";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?," +
            " name = ?, birthday = ? WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday) " +
            "VALUES (?, ?, ?, ?)";
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friendships(user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
    private static final String GET_USER_FRIENDS_QUERY = " SELECT u.*" +
            "        FROM users u" +
            "        JOIN friendships f ON u.id = f.friend_id" +
            "        WHERE f.user_id = ?";
    private static final String GET_COMMON_FRIENDS_QUERY = "SELECT DISTINCT u.*" +
            "        FROM users u" +
            "        JOIN friendships f1 ON u.id = f1.friend_id" +
            "        JOIN friendships f2 ON u.id = f2.friend_id" +
            "        WHERE f1.user_id = ? AND f2.user_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    public Optional<User> getUserById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public List<User> getUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    public User create(User user) {
        long id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    public User update(User user) {
        update(UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    public boolean deleteUser(long id) {
        return delete(DELETE_USER_QUERY, id);
    }

    public void addFriend(long userId, long friendId) {
        jdbc.update(ADD_FRIEND_QUERY, userId, friendId);
    }

    public boolean deleteFriend(long userId, long friendId) {
        int deletedRows = jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
        return deletedRows > 0;
    }

    public HashSet<User> getUserFriends(long userId) {
        return new HashSet<>(findMany(GET_USER_FRIENDS_QUERY, userId));
    }

    public HashSet<User> getCommonFriends(long id, long otherId) {
        return new HashSet<>(findMany(GET_COMMON_FRIENDS_QUERY, id, otherId));
    }

}
