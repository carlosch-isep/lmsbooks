package pt.psoft.g1.psoftg1.bookmanagement.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.BookMongo;

import java.util.List;
import java.util.Optional;

public interface BookMongoRepository extends MongoRepository<BookMongo, String> {
    Optional<BookMongo> findByIsbn_Isbn(String isbn);
    List<BookMongo> findByGenre_Name(String genre);
    List<BookMongo> findByTitle_Title(String title);
    List<BookMongo> findByAuthors_Name(String name);
}
