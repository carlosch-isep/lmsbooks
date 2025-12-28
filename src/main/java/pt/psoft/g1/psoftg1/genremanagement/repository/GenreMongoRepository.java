package pt.psoft.g1.psoftg1.genremanagement.repository;


import pt.psoft.g1.psoftg1.genremanagement.model.GenreMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.context.annotation.Profile;

// Add custom MongoDB queries if needed
@Profile("command")
public interface GenreMongoRepository extends MongoRepository<GenreMongo, String> {
}
