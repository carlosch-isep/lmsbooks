package pt.psoft.g1.psoftg1.sagamanagement.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import pt.psoft.g1.psoftg1.sagamanagement.model.SagaMongo;

@Profile("mongodb")
public interface SagaMongoRepository extends MongoRepository<SagaMongo, String> {
    // Add custom MongoDB queries if needed
}

