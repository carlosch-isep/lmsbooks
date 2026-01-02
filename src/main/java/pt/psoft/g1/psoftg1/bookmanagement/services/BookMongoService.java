package pt.psoft.g1.psoftg1.bookmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQPMapper;
import pt.psoft.g1.psoftg1.bookmanagement.model.*;
import pt.psoft.g1.psoftg1.bookmanagement.publishers.BookEventsPublisher;
import pt.psoft.g1.psoftg1.bookmanagement.repository.BookMongoRepository;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.model.GenreMongo;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.AuthorMongo;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// This service handles all command-side (write) operations for Books using MongoDB.
@Service
@Profile("command")
public class BookMongoService implements BookService {
    @Autowired
    private BookMongoRepository bookMongoRepository;
    @Autowired
    private BookEventsPublisher bookEventsPublisher;
    @Autowired
    private BookViewAMQPMapper bookViewAMQPMapper;

    // --- Conversion helpers ---
    private BookMongo toBookMongo(CreateBookRequest resource, String isbn) {
        BookMongo mongo = new BookMongo();
        mongo.setIsbn(new Isbn(isbn));
        mongo.setTitle(new Title(resource.getTitle()));
        mongo.setDescription(new Description(resource.getDescription()));
        mongo.setGenre(new GenreMongo(null, resource.getGenre()));
        List<AuthorMongo> authors = resource.getAuthors().stream()
                .map(a -> new AuthorMongo(null, a.getName(), a.getBio()))
                .collect(Collectors.toList());
        mongo.setAuthors(authors);
        return mongo;
    }

    private Genre toGenre(GenreMongo genreMongo) {
        if (genreMongo == null) return null;
        return new pt.psoft.g1.psoftg1.genremanagement.model.Genre(genreMongo.getName());
    }

    private List<Author> toAuthors(List<AuthorMongo> authorMongos) {
        if (authorMongos == null) return null;
        return authorMongos.stream()
            .map(am -> new Author(am.getName(), am.getBio(), null))
            .collect(Collectors.toList());
    }

    private Book toBook(BookMongo mongo) {
        if (mongo == null) return null;

        List<Author> authors = toAuthors(mongo.getAuthors());
        if(authors.isEmpty()){
            authors.add(new Author(
                    "Unknown Author",
                    "Unknown Biography",
                    null
            ));
        }

        // Use Book constructor for accessible fields
        return new Book(
            mongo.getIsbn() != null ? mongo.getIsbn().toString() : null,
            mongo.getTitle() != null ? mongo.getTitle().toString() : null,
            mongo.getDescription() != null ? mongo.getDescription().toString() : null,
            toGenre(mongo.getGenre()),
                authors,
            null  // PhotoURI mapping can be improved
        );
    }

    @Override
    public Book create(CreateBookRequest resource, String isbn) {
        BookMongo mongo = toBookMongo(resource, isbn);
        BookMongo saved = bookMongoRepository.save(mongo);
        Book book = toBook(saved);
        bookEventsPublisher.sendBookCreated(book);
        return book;
    }

    @Override
    public Book create(BookViewAMQP bookViewAMQP) {
        // Prevent duplicate books: check if exists
        if (findByIsbn(bookViewAMQP.getIsbn()) != null) {
            throw new IllegalStateException("Book already exists with ISBN: " + bookViewAMQP.getIsbn());
        }
        BookMongo mongo = new BookMongo();
        mongo.setIsbn(new Isbn(bookViewAMQP.getIsbn()));
        mongo.setTitle(new Title(bookViewAMQP.getTitle()));
        mongo.setDescription(new Description(bookViewAMQP.getDescription()));
        mongo.setGenre(new GenreMongo(null, bookViewAMQP.getGenre()));
        // Authors mapping skipped for brevity
        BookMongo saved = bookMongoRepository.save(mongo);
        return toBook(saved);
    }

    @Override
    public Book findByIsbn(String isbn) {
        Optional<BookMongo> mongo = bookMongoRepository.findByIsbn_Isbn(isbn);
        return mongo.map(this::toBook).orElse(null);
    }

    @Override
    public Book update(UpdateBookRequest resource, Long currentVersion) {
        Optional<BookMongo> opt = bookMongoRepository.findByIsbn_Isbn(resource.getIsbn());
        if (opt.isEmpty()) return null;
        BookMongo mongo = opt.get();
        mongo.setTitle(new Title(resource.getTitle()));
        mongo.setDescription(new Description(resource.getDescription()));
        mongo.setGenre(new GenreMongo(null, resource.getGenre()));
        List<AuthorMongo> authors = resource.getAuthors().stream()
                .map(a -> new AuthorMongo(null, String.valueOf(a), "Lorem ipsum dolor sit amet, consectetur adipiscing elit."))
                .collect(Collectors.toList());
        mongo.setAuthors(authors);
        BookMongo saved = bookMongoRepository.save(mongo);
        return toBook(saved);
    }

    @Override
    public Book update(BookViewAMQP bookViewAMQP) {
        Optional<BookMongo> opt = bookMongoRepository.findByIsbn_Isbn(bookViewAMQP.getIsbn());
        if (opt.isEmpty()) return null;
        BookMongo mongo = opt.get();
        mongo.setTitle(new Title(bookViewAMQP.getTitle()));
        mongo.setDescription(new Description(bookViewAMQP.getDescription()));
        mongo.setGenre(new GenreMongo(null, bookViewAMQP.getGenre()));
        // Authors mapping skipped for brevity
        BookMongo saved = bookMongoRepository.save(mongo);
        return toBook(saved);
    }

    @Override
    public List<Book> findByGenre(String genre) {
        return bookMongoRepository.findByGenre_Name(genre).stream().map(this::toBook).collect(Collectors.toList());
    }

    @Override
    public List<Book> findByTitle(String title) {
        return bookMongoRepository.findByTitle_Title(title).stream().map(this::toBook).collect(Collectors.toList());
    }

    @Override
    public List<Book> findByAuthorName(String authorName) {
        return bookMongoRepository.findByAuthors_Name(authorName).stream().map(this::toBook).collect(Collectors.toList());
    }

    @Override
    public Book removeBookPhoto(String isbn, long desiredVersion) {
        Optional<BookMongo> opt = bookMongoRepository.findByIsbn_Isbn(isbn);
        if (opt.isEmpty()) return null;
        BookMongo mongo = opt.get();
        // Assuming BookMongo has a photo field, set it to null (not shown in model)
        // mongo.setPhoto(null);
        BookMongo saved = bookMongoRepository.save(mongo);
        return toBook(saved);
    }

    @Override
    public List<Book> searchBooks(Page page, SearchBooksQuery query) {
        // For demo: return all books (pagination not implemented)
        return bookMongoRepository.findAll().stream().map(this::toBook).collect(Collectors.toList());
    }

    // --- Direct MongoDB access for BookMongoController ---
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
