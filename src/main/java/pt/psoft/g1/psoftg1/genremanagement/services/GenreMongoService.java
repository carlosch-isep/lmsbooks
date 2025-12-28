package pt.psoft.g1.psoftg1.genremanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.GenreMongo;
import pt.psoft.g1.psoftg1.genremanagement.repository.GenreMongoRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Profile("mongodb")
public class GenreMongoService implements GenreService {
    @Autowired
    private GenreMongoRepository genreMongoRepository;

    // --- Mapping methods ---
    private Genre toGenre(GenreMongo mongo) {
        if (mongo == null) return null;
        Genre genre = null;
        try {
            java.lang.reflect.Constructor<Genre> ctor = Genre.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            genre = ctor.newInstance();
        } catch (Exception e) {
            return null;
        }
        if (mongo.getName() != null) {
            try { java.lang.reflect.Field f = Genre.class.getDeclaredField("genre"); f.setAccessible(true); f.set(genre, mongo.getName()); } catch (Exception ignored) {}
        }
        return genre;
    }
    private GenreMongo toGenreMongo(Genre genre) {
        if (genre == null) return null;
        return new GenreMongo(null, genre.toString());
    }

    @Override
    public Iterable<Genre> findAll() {
        return genreMongoRepository.findAll().stream().map(this::toGenre).collect(Collectors.toList());
    }

    @Override
    public Genre save(Genre genre) {
        GenreMongo mongo = toGenreMongo(genre);
        GenreMongo saved = genreMongoRepository.save(mongo);
        return toGenre(saved);
    }

    @Override
    public Optional<Genre> findByString(String name) {
        return genreMongoRepository.findAll().stream().filter(g -> g.getName().equalsIgnoreCase(name)).findFirst().map(this::toGenre);
    }
}
