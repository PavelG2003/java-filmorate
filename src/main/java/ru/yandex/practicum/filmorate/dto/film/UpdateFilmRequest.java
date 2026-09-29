package ru.yandex.practicum.filmorate.dto.film;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateFilmRequest {
    private String name;

    @Size(max = 200)
    private String description;

    private LocalDate releaseDate;

    @Min(1)
    private Integer duration;

    public boolean hasName() {
        return ! (name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return ! (description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return ! (releaseDate == null);
    }

    public boolean hasDuration() {
        return ! (duration == null);
    }
}
