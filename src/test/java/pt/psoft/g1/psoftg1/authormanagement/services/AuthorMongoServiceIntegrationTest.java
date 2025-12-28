package pt.psoft.g1.psoftg1.authormanagement.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repository.AuthorMongoRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("command")
class AuthorMongoServiceIntegrationTest {
    @Autowired
    private AuthorMongoService authorMongoService;
    @Autowired
    private AuthorMongoRepository authorMongoRepository;

    @Test
    void testCreateAndFindAll() {
        // Clean up
        authorMongoRepository.deleteAll();
        // Create
        var req = new CreateAuthorRequest();
        req.setName("Polyglot Author");
        Author created = authorMongoService.create(req);
        assertNotNull(created);
        assertEquals("Polyglot Author", created.getName().toString());
        // Find all
        var all = authorMongoService.findAll();
        assertTrue(all.iterator().hasNext());
    }
}

