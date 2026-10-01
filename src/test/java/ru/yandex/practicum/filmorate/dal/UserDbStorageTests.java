package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

@JdbcTest
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTests {
    @Autowired
    private UserDbStorage storage;

    private User create(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName("Name " + login);
        user.setBirthday(LocalDate.of(1995, 4, 12));
        return storage.create(user);
    }

    @Test
    void createsAndReadsAllFields() {
        User user = create("first");
        assertThat(user.getId()).isPositive();
        assertThat(storage.getUserById(user.getId())).contains(user);
    }

    @Test
    void returnsEmptyForMissingUser() {
        assertThat(storage.getUserById(-1)).isEmpty();
    }

    @Test
    void listsUsers() {
        assertThat(storage.getUsers()).isEmpty();
        User first = create("first");
        User second = create("second");
        assertThat(storage.getUsers()).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void updatesAllFieldsWithoutChangingOtherUsers() {
        User user = create("first");
        User other = create("second");
        user.setEmail("updated@example.com");
        user.setLogin("updated");
        user.setName("Updated");
        user.setBirthday(LocalDate.of(2000, 2, 29));
        storage.update(user);
        assertThat(storage.getUserById(user.getId())).contains(user);
        assertThat(storage.getUserById(other.getId())).contains(other);
    }

    @Test
    void rejectsUpdateOfMissingUser() {
        User user = create("first");
        user.setId(-1L);
        assertThatThrownBy(() -> storage.update(user))
                .isInstanceOf(org.apache.logging.log4j.util.InternalException.class);
    }

    @Test
    void deletesUserAndReportsMissingUser() {
        User user = create("first");
        assertThat(storage.deleteUser(user.getId())).isTrue();
        assertThat(storage.getUserById(user.getId())).isEmpty();
        assertThat(storage.deleteUser(user.getId())).isFalse();
    }

    @Test
    void addsDirectedFriendship() {
        User user = create("first");
        User friend = create("second");
        storage.addFriend(user.getId(), friend.getId());
        assertThat(storage.getUserFriends(user.getId())).containsExactly(friend);
        assertThat(storage.getUserFriends(friend.getId())).isEmpty();
    }

    @Test
    void deletesOnlyRequestedFriendship() {
        User user = create("first");
        User friend = create("second");
        User other = create("third");
        storage.addFriend(user.getId(), friend.getId());
        storage.addFriend(user.getId(), other.getId());
        storage.addFriend(friend.getId(), user.getId());
        assertThat(storage.deleteFriend(user.getId(), friend.getId())).isTrue();
        assertThat(storage.deleteFriend(user.getId(), friend.getId())).isFalse();
        assertThat(storage.getUserFriends(user.getId())).containsExactly(other);
        assertThat(storage.getUserFriends(friend.getId())).containsExactly(user);
    }

    @Test
    void findsOnlyCommonFriends() {
        User first = create("first");
        User second = create("second");
        User common = create("common");
        User exclusive = create("exclusive");
        storage.addFriend(first.getId(), common.getId());
        storage.addFriend(second.getId(), common.getId());
        storage.addFriend(first.getId(), exclusive.getId());
        assertThat(storage.getCommonFriends(first.getId(), second.getId())).containsExactly(common);
        assertThat(storage.getCommonFriends(first.getId(), exclusive.getId())).isEmpty();
    }

    @Test
    void rejectsInvalidFriendships() {
        User user = create("first");
        User friend = create("second");
        storage.addFriend(user.getId(), friend.getId());
        assertThatThrownBy(() -> storage.addFriend(user.getId(), friend.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> storage.addFriend(user.getId(), user.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> storage.addFriend(user.getId(), -1))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
