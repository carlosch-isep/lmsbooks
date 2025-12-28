package pt.psoft.g1.psoftg1.authormanagement.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import pt.psoft.g1.psoftg1.authormanagement.model.AuthorMongo;

@Profile("mongodb")
public interface AuthorMongoRepository extends MongoRepository<AuthorMongo, String> {
    // Add custom MongoDB queries if needed
}

