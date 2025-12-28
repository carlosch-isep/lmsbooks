package pt.psoft.g1.psoftg1.bookmanagement.infrastructure.listeners;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookService;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;

@Component
@Profile("command")
public class BookCreatedEventListener {
    @Autowired
    private BookService bookService;

    @RabbitListener(queues = "book.created.queue") // Use your actual queue name
    public void handleBookCreated(BookViewAMQP bookViewAMQP) {
        // Check if book already exists by ISBN
        Book existing = null;
        try {
            existing = bookService.findByIsbn(bookViewAMQP.getIsbn());
        } catch (Exception ignored) {}
        if (existing != null) {
            // Book already exists, ignore event
            return;
        }
        // Create the book in the database
        bookService.create(bookViewAMQP);
    }
}

