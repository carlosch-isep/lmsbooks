package pt.psoft.g1.psoftg1.bookmanagement.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.psoft.g1.psoftg1.bookmanagement.model.BookMongo;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookMongoService;

import java.util.List;

@RestController
@RequestMapping("/mongo/books")
public class BookMongoController {
    @Autowired
    private BookMongoService bookMongoService;

    @PostMapping
    public ResponseEntity<BookMongo> create(@RequestBody BookMongo book) {
        // Direct save is not available; implement a createBookMongo method or use repository directly if needed
        // For now, return 501 Not Implemented
        return ResponseEntity.status(501).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookMongo> getById(@PathVariable String id) {
        return bookMongoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<BookMongo> getAll() {
        return bookMongoService.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        bookMongoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
