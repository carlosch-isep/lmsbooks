package pt.psoft.g1.psoftg1.authormanagement.services;

import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.model.AuthorMongo;
import pt.psoft.g1.psoftg1.shared.model.Name;

import static org.junit.jupiter.api.Assertions.*;

class AuthorMongoServiceTest {
    private final AuthorMongoService service = new AuthorMongoService();

    @Test
    void testToAuthorMongoAndBack() throws Exception {
        // Create Author
        Author author = createAuthor(42L, "Jane Doe");
        AuthorMongo mongo = invokeToAuthorMongo(service, author);
        assertEquals("42", mongo.getId());
        assertEquals("Jane Doe", mongo.getName());
        // Back to Author
        Author author2 = invokeToAuthor(service, mongo);
        assertNotNull(author2);
        assertEquals(42L, author2.getId());
        assertEquals("Jane Doe", author2.getName().toString());
    }

    // Helper to create Author via reflection
    private Author createAuthor(Long id, String name) throws Exception {
        java.lang.reflect.Constructor<Author> ctor = Author.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Author author = ctor.newInstance();
        java.lang.reflect.Field fName = Author.class.getDeclaredField("name");
        fName.setAccessible(true);
        fName.set(author, new Name(name));
        java.lang.reflect.Field fId = Author.class.getDeclaredField("authorNumber");
        fId.setAccessible(true);
        fId.set(author, id);
        return author;
    }

    // Use reflection to call private mapping methods
    private AuthorMongo invokeToAuthorMongo(AuthorMongoService service, Author author) throws Exception {
        var m = AuthorMongoService.class.getDeclaredMethod("toAuthorMongo", Author.class);
        m.setAccessible(true);
        return (AuthorMongo) m.invoke(service, author);
    }
    private Author invokeToAuthor(AuthorMongoService service, AuthorMongo mongo) throws Exception {
        var m = AuthorMongoService.class.getDeclaredMethod("toAuthor", AuthorMongo.class);
        m.setAccessible(true);
        return (Author) m.invoke(service, mongo);
    }
}

