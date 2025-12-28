package pt.psoft.g1.psoftg1.bookmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Message;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookService;

import static org.mockito.Mockito.*;

class BookRabbitmqControllerTest {
    @Mock
    private BookService bookService;
    @InjectMocks
    private BookRabbitmqController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    void receiveBookCreatedMsg_shouldCreateBook() throws Exception {
        BookViewAMQP bookViewAMQP = new BookViewAMQP();
        bookViewAMQP.setIsbn("9789895612864");
        bookViewAMQP.setTitle("Test Book");
        bookViewAMQP.setDescription("Test Description");
        bookViewAMQP.setGenre("Fiction");
        // Add more fields as needed

        String json = objectMapper.writeValueAsString(bookViewAMQP);
        Message message = new Message(json.getBytes());

        controller.receiveBookCreatedMsg(message);

        verify(bookService, times(1)).create(any(BookViewAMQP.class));
    }

    @Test
    void receiveBookCreatedMsg_shouldHandleDuplicateBook() throws Exception {
        BookViewAMQP bookViewAMQP = new BookViewAMQP();
        bookViewAMQP.setIsbn("9789895612864");
        bookViewAMQP.setTitle("Test Book");
        bookViewAMQP.setDescription("Test Description");
        bookViewAMQP.setGenre("Fiction");
        String json = objectMapper.writeValueAsString(bookViewAMQP);
        Message message = new Message(json.getBytes());

        doThrow(new IllegalStateException("Book already exists with ISBN: 9789895612864"))
                .when(bookService).create(any(BookViewAMQP.class));

        controller.receiveBookCreatedMsg(message);

        verify(bookService, times(1)).create(any(BookViewAMQP.class));
    }
}

