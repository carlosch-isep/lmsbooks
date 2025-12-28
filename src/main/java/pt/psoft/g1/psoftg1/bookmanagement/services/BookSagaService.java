package pt.psoft.g1.psoftg1.bookmanagement.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.services.AuthorService;
import pt.psoft.g1.psoftg1.authormanagement.services.CreateAuthorRequest;
import pt.psoft.g1.psoftg1.sagamanagement.model.Saga;
import pt.psoft.g1.psoftg1.sagamanagement.services.SagaService;
import pt.psoft.g1.psoftg1.shared.enums.SagaStatus;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.sagamanagement.model.BookSagaDependency;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BookSagaService {
    @Autowired
    private SagaService sagaService;
    @Autowired
    private BookService bookService;
    @Autowired
    private AuthorService authorService;
    @Autowired
    private BookSagaDependency bookSagaDependency;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UUID createBookWithSaga(BookViewAMQP bookViewAMQP) {
        // Convert BookViewAMQP to CreateBookRequest for dependency check
        CreateBookRequest createBookRequest = convertToCreateBookRequest(bookViewAMQP);
        bookSagaDependency.setRequest(createBookRequest);
        SagaStatus initialStatus = bookSagaDependency.areDependenciesResolved() ? SagaStatus.PENDING : SagaStatus.WAITING_DEPENDENCIES;
        String objectData;
        try {
            objectData = objectMapper.writeValueAsString(bookViewAMQP);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize book data", e);
        }
        Saga saga = new Saga(initialStatus, BookViewAMQP.class.getName(), objectData);
        UUID sagaId = UUID.randomUUID();
        saga.setSagaId(sagaId);
        sagaService.saveSaga(saga);
        if (initialStatus == SagaStatus.WAITING_DEPENDENCIES) {
            return sagaId;
        }
        try {
            bookService.create(bookViewAMQP);
            saga.setStatus(SagaStatus.APPROVED);
        } catch (Exception e) {
            saga.setStatus(SagaStatus.REJECTED);
        }
        sagaService.updateSaga(saga);
        return sagaId;
    }

    private CreateBookRequest convertToCreateBookRequest(BookViewAMQP bookViewAMQP) {
        // For now, return a dummy object or map fields as needed

        CreateBookRequest request = new CreateBookRequest();
        request.setIsbn(bookViewAMQP.getIsbn());
        request.setTitle(bookViewAMQP.getTitle());
        request.setDescription(bookViewAMQP.getDescription());
        request.setGenre(bookViewAMQP.getGenre());

        List<CreateAuthorRequest> authorList = new ArrayList<>();

        for (Long id : bookViewAMQP.getAuthorIds()) {
            Author author = authorService.findCoAuthorsByAuthorNumber(id).getFirst();
            authorList.add(new CreateAuthorRequest(
                    author.getName(),
                    author.getBio(),
                    null,
                    author.getPhoto().getPhotoFile()
            ));
        }


        request.setAuthors(authorList);
        return request;
    }
}
