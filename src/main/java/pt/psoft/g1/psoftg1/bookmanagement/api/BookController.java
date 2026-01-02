package pt.psoft.g1.psoftg1.bookmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.services.*;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.FileStorageService;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@Profile("query")
@RequestMapping("/api/query/books")
@RequiredArgsConstructor
public class BookController {

    @Autowired
    private BookServiceImpl bookQueryService;

    private final FileStorageService fileStorageService;
    private final BookViewMapper bookViewMapper;

    @Operation(summary = "Gets Books by title or genre")
    @GetMapping
    public ListResponse<BookView> findBooks(@RequestParam(value = "title", required = false) final String title,
                                            @RequestParam(value = "genre", required = false) final String genre,
                                            @RequestParam(value = "authorName", required = false) final String authorName) {

        List<Book> booksByTitle = null;
        if (title != null)
            booksByTitle = bookQueryService.findByTitle(title);

        List<Book> booksByGenre = null;
        if (genre != null)
            booksByGenre = bookQueryService.findByGenre(genre);

        List<Book> booksByAuthorName = null;
        if (authorName != null)
            booksByAuthorName = bookQueryService.findByAuthorName(authorName);

        Set<Book> bookSet = new HashSet<>();
        if (booksByTitle != null)
            bookSet.addAll(booksByTitle);
        if (booksByGenre != null)
            bookSet.addAll(booksByGenre);
        if (booksByAuthorName != null)
            bookSet.addAll(booksByAuthorName);

        List<Book> books = bookSet.stream().sorted(Comparator.comparing(b -> b.getTitle().toString()))
                .collect(Collectors.toList());

        if (books.isEmpty())
            return new ListResponse<>(new ArrayList<>());

        return new ListResponse<>(bookViewMapper.toBookView(books));
    }

    @Operation(summary = "Gets a specific Book by isbn")
    @GetMapping(value = "/{isbn}")
    public ResponseEntity<BookView> findByIsbn(@PathVariable final String isbn) {
        if (bookQueryService == null) throw new IllegalStateException("Query service not available");
        final var book = bookQueryService.findByIsbn(isbn);

        BookView bookView = bookViewMapper.toBookView(book);

        return ResponseEntity.ok().eTag(Long.toString(book.getVersion())).body(bookView);
    }

    @Operation(summary = "Gets a book photo")
    @GetMapping("/{isbn}/photo")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> getSpecificBookPhoto(@PathVariable("isbn") final String isbn) {
        if (bookQueryService == null) throw new IllegalStateException("Query service not available");
        Book book = bookQueryService.findByIsbn(isbn);

        // In case the user has no photo, just return a 200 OK without body
        if (book.getPhoto() == null) {
            return ResponseEntity.ok().build();
        }

        String photoFile = book.getPhoto().getPhotoFile();
        byte[] image = fileStorageService.getFile(photoFile);
        String fileFormat = fileStorageService.getExtension(book.getPhoto().getPhotoFile())
                .orElseThrow(() -> new ValidationException("Unable to get file extension"));

        if (image == null) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.ok().contentType(fileFormat.equals("png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG)
                .body(image);

    }


}
