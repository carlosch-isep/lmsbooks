package pt.psoft.g1.psoftg1.authormanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.AuthorMongo;
import pt.psoft.g1.psoftg1.authormanagement.repository.AuthorMongoRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Profile("mongodb")
public class AuthorMongoService implements AuthorService {
    @Autowired
    private AuthorMongoRepository authorMongoRepository;

    // --- Mapping methods ---
    private Author toAuthor(AuthorMongo mongo) {
        if (mongo == null) return null;
        Author author = null;
        try {
            java.lang.reflect.Constructor<Author> ctor = Author.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            author = ctor.newInstance();
        } catch (Exception e) {
            return null;
        }
        // Use reflection to set private fields if needed
        if (mongo.getName() != null) {
            try { java.lang.reflect.Field f = Author.class.getDeclaredField("name"); f.setAccessible(true); f.set(author, new pt.psoft.g1.psoftg1.shared.model.Name(mongo.getName())); } catch (Exception ignored) {}
        }
        if (mongo.getId() != null) {
            try { java.lang.reflect.Field f = Author.class.getDeclaredField("authorNumber"); f.setAccessible(true); f.set(author, Long.parseLong(mongo.getId())); } catch (Exception ignored) {}
        }
        return author;
    }
    private AuthorMongo toAuthorMongo(Author author) {
        if (author == null) return null;
        AuthorMongo mongo = new AuthorMongo();
        if (author.getId() != null) mongo.setId(String.valueOf(author.getId()));
        if (author.getName() != null) mongo.setName(author.getName().toString());
        return mongo;
    }

    @Override
    public Iterable<Author> findAll() {
        return authorMongoRepository.findAll().stream().map(this::toAuthor).collect(Collectors.toList());
    }

    @Override
    public Optional<Author> findByAuthorNumber(Long authorNumber) {
        return authorMongoRepository.findAll().stream()
                .filter(a -> a.getId() != null && a.getId().equals(authorNumber.toString()))
                .findFirst().map(this::toAuthor);
    }

    @Override
    public List<Author> findByName(String name) {
        return authorMongoRepository.findAll().stream()
                .filter(a -> a.getName() != null && a.getName().equalsIgnoreCase(name))
                .map(this::toAuthor)
                .collect(Collectors.toList());
    }

    @Override
    public Author create(CreateAuthorRequest resource) {
        AuthorMongo mongo = new AuthorMongo();
        // Map fields from resource to mongo (implement as needed)
        if (resource.getName() != null) mongo.setName(resource.getName());
        AuthorMongo saved = authorMongoRepository.save(mongo);
        return toAuthor(saved);
    }

    @Override
    public Author partialUpdate(Long authorNumber, UpdateAuthorRequest resource, long desiredVersion) {
        // Implement update logic for MongoDB (not implemented)
        return null;
    }

    @Override
    public List<Book> findBooksByAuthorNumber(Long authorNumber) {
        // Implement logic for MongoDB (not implemented)
        return null;
    }

    @Override
    public List<Author> findCoAuthorsByAuthorNumber(Long authorNumber) {
        // Implement logic for MongoDB (not implemented)
        return null;
    }

    @Override
    public Optional<Author> removeAuthorPhoto(Long authorNumber, long desiredVersion) {
        // Implement logic for MongoDB (not implemented)
        return Optional.empty();
    }
}
