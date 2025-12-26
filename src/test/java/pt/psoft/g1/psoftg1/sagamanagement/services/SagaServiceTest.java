package pt.psoft.g1.psoftg1.sagamanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import pt.psoft.g1.psoftg1.TestConfig;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.sagamanagement.entities.SagaEntity;
import pt.psoft.g1.psoftg1.sagamanagement.model.Saga;
import pt.psoft.g1.psoftg1.sagamanagement.repositories.SagaRepository;
import pt.psoft.g1.psoftg1.shared.enums.SagaStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {TestConfig.class},
        properties = {
                "stubrunner.amqp.mockConnection=true",
                "spring.profiles.active=test,postgres"
        }
)
public class SagaServiceTest {

    @MockBean
    private SagaRepository sagaRepository;

    private final UUID exists = UUID.randomUUID();

    private final UUID exists_updated = UUID.randomUUID();

    private final UUID dontExists = UUID.randomUUID();

    SagaEntity element;

    SagaEntity element_changed;

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

        element = new SagaEntity(new Saga(
                exists,
                SagaStatus.PENDING,
                new ObjectMapper().writeValueAsString(example),
                Book.class.getName()
        ));

        element_changed =  new SagaEntity(new Saga(
                exists_updated,
                SagaStatus.PENDING,
                new ObjectMapper().writeValueAsString(example),
                Book.class.getName()
        ));

        SagaEntity element_changed_updated = new SagaEntity(new Saga(
                exists_updated,
                SagaStatus.APPROVED,
                new ObjectMapper().writeValueAsString(example),
                Book.class.getName()
        ));

        // Find test
        Mockito.when(
                this.sagaRepository.findById(element.getSagaId())
        ).thenReturn(
                Optional.of(element)
        );

        Mockito.when(
                this.sagaRepository.findById(element_changed.getSagaId())
        ).thenReturn(
                Optional.of(element_changed)
        );

        // Create test
        Mockito.when(
                sagaRepository.save(element)
        ).thenReturn(
                element
        );

        // Change Status test
        Mockito.when(
                sagaRepository.save(element_changed)
        ).thenReturn(
                element_changed_updated
        );
    }

    @Test
    public void whenValidId_thenSagaShouldBeFound(){
        Optional<SagaEntity> exists = this.sagaRepository.findById(this.exists.toString());
        exists.ifPresent(saga -> assertThat(saga.getObjectData()).isEqualTo(this.element.getObjectData()));
    }

    @Test
    public void whenValidId_thenSagaShouldNotBeFound(){
        Optional<SagaEntity> exists = sagaRepository.findById(this.dontExists.toString());
        exists.ifPresent(saga -> assertThat(saga).isEqualTo(null));
    }

    @Test
    public void whenValid_saveSagaShouldBeSaved(){
        SagaEntity saved = sagaRepository.save(element);
        assertThat(saved).isEqualTo(element);
    }

    @Test
    public void whenValid_saveSagaShouldBeUpdated(){
        SagaEntity update = sagaRepository.findById(this.exists_updated.toString()).get();
        update.setStatus(SagaStatus.APPROVED);
        SagaEntity saved = sagaRepository.save(update);
        assertThat(saved.getStatus()).isEqualTo(SagaStatus.APPROVED);
    }

}
