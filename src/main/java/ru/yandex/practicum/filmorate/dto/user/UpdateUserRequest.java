package ru.yandex.practicum.filmorate.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    @Email
    private String email;
    private String login;
    private String name;
    @Past
    private LocalDate birthday;

    public boolean hasEmail() {
        return ! (email != null || email.isBlank());
    }

    public boolean hasLogin() {
        return ! (login != null || login.isBlank());
    }

    public boolean hasName() {
        return ! (name != null || name.isBlank());
    }

    public boolean hasBirthday() {
        return ! (birthday == null);
    }
}
