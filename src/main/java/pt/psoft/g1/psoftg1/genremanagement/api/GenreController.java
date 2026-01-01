package pt.psoft.g1.psoftg1.genremanagement.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;

import java.util.Optional;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService genreService;

    @Autowired
    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public Iterable<Genre> getAllGenres() {
        return genreService.findAll();
    }

    @PostMapping
    public Genre createGenre(@RequestBody Genre genre) {
        return genreService.save(genre);
    }

    // Exemplo de endpoint para buscar por nome, se implementado no service
    @GetMapping("/{name}")
    public Optional<Genre> getGenreByName(@PathVariable String name) {
        try {
            return genreService.findByString(name);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

