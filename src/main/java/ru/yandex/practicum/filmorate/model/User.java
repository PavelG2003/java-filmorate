package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String login;
    private String name;

    @NotNull
    @Past
    private LocalDate birthday;

    private Set<User> friends = new HashSet<>();
}
