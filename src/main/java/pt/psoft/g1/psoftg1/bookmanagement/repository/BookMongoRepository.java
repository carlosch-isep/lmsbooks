package pt.psoft.g1.psoftg1.bookmanagement.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.BookMongo;

public interface BookMongoRepository extends MongoRepository<BookMongo, String> {
    // Add custom MongoDB queries if needed
}

