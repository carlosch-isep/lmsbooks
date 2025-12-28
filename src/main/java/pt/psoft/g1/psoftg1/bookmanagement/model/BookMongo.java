package pt.psoft.g1.psoftg1.bookmanagement.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.psoft.g1.psoftg1.authormanagement.model.AuthorMongo;
import pt.psoft.g1.psoftg1.genremanagement.model.GenreMongo;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Profile("mongodb")
@Document(collection = "book")
public class BookMongo {
    @Id
    private String id; // Use String for MongoDB ObjectId

    @Version
    private Long version;

    private Isbn isbn;
    private Title title;
    private GenreMongo genre;
    private List<AuthorMongo> authors = new ArrayList<>();
    private Description description;

    // No JPA annotations here; this class is for MongoDB only
}
