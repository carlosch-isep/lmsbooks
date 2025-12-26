package pt.psoft.g1.psoftg1.sagamanagement.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.shared.enums.SagaStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SagaTest {

    private String data;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        // Create list of authors
        List<Author> lstAuthors = new ArrayList<>();
        lstAuthors.add(new Author("John Doe", "Lorem Ipsum", ""));

        // Create book with a valid ISBN
        Book example = new Book(
                "9782826012092",
                "Example book",
                "Lorem Ipsum",
                new Genre("Example"),
                lstAuthors,
                ""
        );

        // Convert to JSON
        data = new ObjectMapper().writeValueAsString(example);
    }
    @AfterEach
    void cleanUp(){
        data = "";
    }

    /**
     * Method created to hold notNull args
     * @return Stream
     */
    Stream<Arguments> notNullDataArgs() {
        return Stream.of(
                Arguments.of(null, data, Book.class.getName()),
                Arguments.of(SagaStatus.PENDING, null, Book.class.getName()),
                Arguments.of(SagaStatus.PENDING, data, null)
        );
    }

    @Test
    void ensureSuccessWithGoodData() {
        Saga instance = new Saga(SagaStatus.PENDING, Book.class.getName(), data);
        assertTrue(
                !instance.getObjectData().isBlank() && !instance.getObjectClass().isBlank()
        );
    }

    @ParameterizedTest
    @MethodSource("notNullDataArgs")
    void ensureFieldsNotNull(SagaStatus status, String className,String data) {
        assertThrows(
                IllegalArgumentException.class, () -> new Saga(status, className ,data)
        );
    }

}
