package pt.psoft.g1.psoftg1.bookmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.BookMongo;
import pt.psoft.g1.psoftg1.bookmanagement.services.*;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.FileStorageService;
import pt.psoft.g1.psoftg1.shared.services.SearchRequest;

import java.util.List;

@RestController
@Profile("command")
@RequestMapping("/api/command/books")
@RequiredArgsConstructor
public class BookMongoController {
    @Autowired
    private BookMongoService bookCommandService;

    private final BookViewMapper bookViewMapper;
    private final FileStorageService fileStorageService;
    private final ConcurrencyService concurrencyService;

    @Operation(summary = "Register a new Book")
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BookView> create(
            @RequestBody @Valid CreateBookRequest resource
    ) {
        // Guarantee that the client doesn't provide a link on the body, null = no photo or error
        resource.setPhotoURI(null);
        MultipartFile file = resource.getPhoto();

        String fileName = fileStorageService.getRequestPhoto(file);

        if (fileName != null) {
            resource.setPhotoURI(fileName);
        }

        Book book;
        if (bookCommandService != null) {
            book = bookCommandService.create(resource, resource.getIsbn());
        } else {
            throw new IllegalStateException("Command service not available");
        }
        // final var savedBook = bookService.save(book);
        final var newBookUri = ServletUriComponentsBuilder.fromCurrentRequestUri().pathSegment(book.getIsbn()).build()
                .toUri();

        return ResponseEntity.created(newBookUri).eTag(Long.toString(book.getVersion()))
                .body(bookViewMapper.toBookView(book));
    }


    @Operation(summary = "Updates a specific Book")
    @PatchMapping(value = "/{isbn}")
    public ResponseEntity<BookView> updateBook(@PathVariable final String isbn, final WebRequest request,
                                               @Valid final UpdateBookRequest resource) {

        final String ifMatchValue = request.getHeader(ConcurrencyService.IF_MATCH);
        if (ifMatchValue == null || ifMatchValue.isEmpty() || ifMatchValue.equals("null")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You must issue a conditional PATCH using 'if-match'");
        }

        MultipartFile file = resource.getPhoto();

        String fileName = fileStorageService.getRequestPhoto(file);

        if (fileName != null) {
            resource.setPhotoURI(fileName);
        }

        Book book;
        resource.setIsbn(isbn);
        if (bookCommandService != null) {
            book = bookCommandService.update(resource, concurrencyService.getVersionFromIfMatchHeader(ifMatchValue));
        } else {
            throw new IllegalStateException("Command service not available");
        }
        return ResponseEntity.ok().eTag(Long.toString(book.getVersion())).body(bookViewMapper.toBookView(book));
    }


    @Operation(summary = "Deletes a book photo")
    @DeleteMapping("/{isbn}/photo")
    public ResponseEntity<Void> deleteBookPhoto(@PathVariable("isbn") final String isbn) {
        var book = bookCommandService.findByIsbn(isbn);
        if (book.getPhoto() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        fileStorageService.deleteFile(book.getPhoto().getPhotoFile());
        bookCommandService.removeBookPhoto(book.getIsbn(), book.getVersion());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/search")
    public ListResponse<BookView> searchBooks(@RequestBody final SearchRequest<SearchBooksQuery> request) {
        if (bookCommandService == null) throw new IllegalStateException("Query service not available");
        final var bookList = bookCommandService.searchBooks(request.getPage(), request.getQuery());
        return new ListResponse<>(bookViewMapper.toBookView(bookList));
    }
}
