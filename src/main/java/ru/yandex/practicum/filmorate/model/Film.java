package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;

/**
 * Film.
 */
@Data
public class Film {
    private Long id;

    @NotBlank
    @NotNull
    private String name;
    private String description;
    private Instant releaseDate;
    private Duration duration;
}
