package pt.psoft.g1.psoftg1.bookmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.BookMongo;
import pt.psoft.g1.psoftg1.bookmanagement.model.Description;
import pt.psoft.g1.psoftg1.bookmanagement.model.Isbn;
import pt.psoft.g1.psoftg1.bookmanagement.repository.BookMongoRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Profile("mongodb")
public class BookMongoService implements BookService {
    @Autowired
    private BookMongoRepository bookMongoRepository;

    // --- Mapping methods ---
    private Book toBook(BookMongo mongo) {
        if (mongo == null) return null;
        Book book = null;
        try {
            java.lang.reflect.Constructor<Book> ctor = Book.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            book = ctor.newInstance();
        } catch (Exception e) {
            return null;
        }
        // Set ISBN
        if (mongo.getIsbn() != null) {
            try { java.lang.reflect.Field f = Book.class.getDeclaredField("isbn"); f.setAccessible(true); f.set(book, new pt.psoft.g1.psoftg1.bookmanagement.model.Isbn(mongo.getIsbn().toString())); } catch (Exception ignored) {}
        }
        // Set Title
        if (mongo.getTitle() != null) {
            try { java.lang.reflect.Field f = Book.class.getDeclaredField("title"); f.setAccessible(true); f.set(book, mongo.getTitle()); } catch (Exception ignored) {}
        }
        // Set Description
        if (mongo.getDescription() != null) {
            try { java.lang.reflect.Field f = Book.class.getDeclaredField("description"); f.setAccessible(true); f.set(book, new pt.psoft.g1.psoftg1.bookmanagement.model.Description(mongo.getDescription().toString())); } catch (Exception ignored) {}
        }
        // Set Genre
        if (mongo.getGenre() != null) {
            try { java.lang.reflect.Field f = Book.class.getDeclaredField("genre"); f.setAccessible(true); f.set(book, toGenre(mongo.getGenre())); } catch (Exception ignored) {}
        }
        // Set Authors
        if (mongo.getAuthors() != null) {
            try { java.lang.reflect.Field f = Book.class.getDeclaredField("authors"); f.setAccessible(true); f.set(book, mongo.getAuthors().stream().map(this::toAuthor).collect(Collectors.toList())); } catch (Exception ignored) {}
        }
        return book;
    }
    private BookMongo toBookMongo(Book book) {
        if (book == null) return null;
        BookMongo mongo = new BookMongo();
        if (book.getIsbn() != null) mongo.setIsbn(new Isbn(book.getIsbn()));
        if (book.getTitle() != null) mongo.setTitle(book.getTitle());
        if (book.getDescription() != null) mongo.setDescription(new Description( book.getDescription()));
        if (book.getGenre() != null) mongo.setGenre(toGenreMongo(book.getGenre()));
        if (book.getAuthors() != null) mongo.setAuthors(book.getAuthors().stream().map(this::toAuthorMongo).collect(Collectors.toList()));
        return mongo;
    }
    private pt.psoft.g1.psoftg1.genremanagement.model.Genre toGenre(pt.psoft.g1.psoftg1.genremanagement.model.GenreMongo genreMongo) {
        if (genreMongo == null) return null;
        return new pt.psoft.g1.psoftg1.genremanagement.model.Genre(genreMongo.getName());
    }
    private pt.psoft.g1.psoftg1.genremanagement.model.GenreMongo toGenreMongo(pt.psoft.g1.psoftg1.genremanagement.model.Genre genre) {
        if (genre == null) return null;
        return new pt.psoft.g1.psoftg1.genremanagement.model.GenreMongo(null, genre.toString());
    }
    private pt.psoft.g1.psoftg1.authormanagement.model.Author toAuthor(pt.psoft.g1.psoftg1.authormanagement.model.AuthorMongo authorMongo) {
        if (authorMongo == null) return null;
        return new pt.psoft.g1.psoftg1.authormanagement.model.Author(authorMongo.getName(), null, null);
    }
    private pt.psoft.g1.psoftg1.authormanagement.model.AuthorMongo toAuthorMongo(pt.psoft.g1.psoftg1.authormanagement.model.Author author) {
        if (author == null) return null;
        return new pt.psoft.g1.psoftg1.authormanagement.model.AuthorMongo(String.valueOf(author.getId()), author.getName().toString());
    }

    @Override
    public Book create(CreateBookRequest request, String isbn) {
        BookMongo bookMongo = new BookMongo();
        // Map fields from request to bookMongo (implement as needed)
        // bookMongo.setIsbn(...); bookMongo.setTitle(...); etc.
        BookMongo saved = bookMongoRepository.save(bookMongo);
        return toBook(saved);
    }

    @Override
    public Book create(BookViewAMQP bookViewAMQP) {
        BookMongo bookMongo = new BookMongo();
        // Map fields from bookViewAMQP to bookMongo (implement as needed)
        BookMongo saved = bookMongoRepository.save(bookMongo);
        return toBook(saved);
    }

    @Override
    public Book findByIsbn(String isbn) {
        return bookMongoRepository.findAll().stream()
                .filter(b -> b.getIsbn() != null && b.getIsbn().toString().equals(isbn))
                .findFirst().map(this::toBook).orElse(null);
    }

    @Override
    public Book update(UpdateBookRequest request, Long currentVersion) {
        // Implement update logic for MongoDB
        return null;
    }

    @Override
    public Book update(BookViewAMQP bookViewAMQP) {
        // Implement update logic for MongoDB
        return null;
    }

    @Override
    public List<Book> findByGenre(String genre) {
        return bookMongoRepository.findAll().stream()
                .filter(b -> b.getGenre() != null && b.getGenre().getName().equalsIgnoreCase(genre))
                .map(this::toBook)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findByTitle(String title) {
        return bookMongoRepository.findAll().stream()
                .filter(b -> b.getTitle() != null && b.getTitle().toString().equalsIgnoreCase(title))
                .map(this::toBook)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findByAuthorName(String authorName) {
        return bookMongoRepository.findAll().stream()
                .filter(b -> b.getAuthors() != null && b.getAuthors().stream().anyMatch(a -> a.getName().equalsIgnoreCase(authorName)))
                .map(this::toBook)
                .collect(Collectors.toList());
    }

    @Override
    public Book removeBookPhoto(String isbn, long desiredVersion) {
        // Implement logic to remove photo from BookMongo
        return null;
    }

    @Override
    public List<Book> searchBooks(Page page, SearchBooksQuery query) {
        // Implement search logic for MongoDB
        return bookMongoRepository.findAll().stream().map(this::toBook).collect(Collectors.toList());
    }

    // Additional MongoDB-specific methods if needed
    public Optional<BookMongo> findById(String id) {
        return bookMongoRepository.findById(id);
    }

    public List<BookMongo> findAll() {
        return bookMongoRepository.findAll();
    }

    public void deleteById(String id) {
        bookMongoRepository.deleteById(id);
    }
}
