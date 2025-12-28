package pt.psoft.g1.psoftg1.bookmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQPMapper;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.BookMongo;
import pt.psoft.g1.psoftg1.bookmanagement.publishers.BookEventsPublisher;
import pt.psoft.g1.psoftg1.bookmanagement.repository.BookMongoRepository;
import pt.psoft.g1.psoftg1.genremanagement.model.GenreMongo;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookMongoServiceTest {
    @Mock
    private BookMongoRepository bookMongoRepository;
    @Mock
    private BookEventsPublisher bookEventsPublisher;
    @Mock
    private BookViewAMQPMapper bookViewAMQPMapper;

    @InjectMocks
    private BookMongoService bookMongoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldSendBookCreatedEvent() {
        // Arrange
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("Test Book");
        request.setDescription("Test Description");
        request.setGenre("Fiction");
        request.setAuthors(Collections.emptyList());
        String isbn = "9789895612864";

        BookMongo savedMongo = new BookMongo();
        savedMongo.setIsbn(new pt.psoft.g1.psoftg1.bookmanagement.model.Isbn(isbn));
        savedMongo.setTitle(new pt.psoft.g1.psoftg1.bookmanagement.model.Title("Test Book"));
        savedMongo.setDescription(new pt.psoft.g1.psoftg1.bookmanagement.model.Description("Test Description"));
        savedMongo.setGenre(new GenreMongo(null, "Fiction"));
        savedMongo.setAuthors(Collections.emptyList());

        when(bookMongoRepository.save(any(BookMongo.class))).thenReturn(savedMongo);

        // Act
        Book result = bookMongoService.create(request, isbn);

        // Assert
        assertNotNull(result);
        verify(bookEventsPublisher, times(1)).sendBookCreated(any(Book.class));
    }
}
