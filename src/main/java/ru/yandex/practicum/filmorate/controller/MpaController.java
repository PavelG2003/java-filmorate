package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Validated
public class MpaController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Mpa> getMpaCollection() {
        return filmService.getMpaCollection();
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable @Positive Long id) {
        return filmService.getMpaById(id);
    }
}
