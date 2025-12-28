package pt.psoft.g1.psoftg1.sagamanagement.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.sagamanagement.entities.SagaEntity;
import pt.psoft.g1.psoftg1.sagamanagement.model.Saga;
import pt.psoft.g1.psoftg1.sagamanagement.repositories.SagaRepository;
import pt.psoft.g1.psoftg1.shared.enums.SagaStatus;

import java.util.List;
import java.util.UUID;

@Service
public class SagaService {

    private final SagaRepository sagaRepository;

    public SagaService(SagaRepository sagaRepository) {
        this.sagaRepository = sagaRepository;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get object by given an sageId
     * @param sagaId UUID saga unique id
     * @param expectedType T return expected class
     * @return Return expected class
     * @param <T> Typing
     * @throws ClassNotFoundException If expected class not found
     * @throws JsonProcessingException If encounter an error while parsing the saving data
     */
    @Transactional
    public <T> T get(UUID sagaId, Class<T> expectedType) throws ClassNotFoundException, JsonProcessingException {
        // Get SagaEntity record
        SagaEntity entity = sagaRepository.findById(sagaId.toString()).orElseThrow(() -> new RuntimeException("Saga not found"));
        Saga record = entity.toDomain();
        // Get class from saga Repo
        Class<?> clazz = Class.forName(record.getObjectClass());
        if (!clazz.getName().equals(expectedType.getName())){
            throw new RuntimeException("Expected type is not equal to the type returned by de repo");
        }
        // Convert objectData to request object
        return this.objectMapper.readValue(record.getObjectData(), expectedType);
    }

    /**
     * Change state, this must be used
     * @param sagaId UUID saga unique id
     * @param newStatus SagaStatus new Saga status
     * @return Saga return saved object
     */
    @Transactional
    public Saga changeStatus(UUID sagaId, SagaStatus newStatus) {
        // Get SagaEntity record
        SagaEntity entity = sagaRepository.findById(sagaId.toString()).orElseThrow(() -> new RuntimeException("Saga not found"));
        entity.setStatus(newStatus);
        return sagaRepository.save(entity).toDomain();
    }

    /**
     * Save Saga information
     * @param objectData T Object saved
     * @param <T> Class
     * @throws JsonProcessingException if encounter an error while convert data to string
     */
    @Transactional
    public <T> Saga set(T objectData) throws JsonProcessingException {
        // Create Saga domain object
        Saga saga = new Saga(
                SagaStatus.PENDING,
                objectData.getClass().getName(),
                this.objectMapper.writeValueAsString(objectData)
        );
        // Save SagaEntity object
        SagaEntity entity = new SagaEntity(saga);
        return sagaRepository.save(entity).toDomain();
    }

    @Transactional
    public List<Saga> getByStatus(SagaStatus status){
        return sagaRepository.findByStatus(status).stream().map(SagaEntity::toDomain).toList();
    }

    @Transactional
    public List<Saga> getByObjectClassAndStatus(String objectClass, SagaStatus status){
        return sagaRepository.findByObjectClassAndStatus(objectClass, status).stream().map(SagaEntity::toDomain).toList();
    }

    @Transactional
    public void saveSaga(Saga saga) {
        SagaEntity entity = new SagaEntity(saga);
        sagaRepository.save(entity).toDomain();
    }

    @Transactional
    public void updateSaga(Saga saga) {
        SagaEntity entity = new SagaEntity(saga);
        sagaRepository.save(entity).toDomain();
    }

}
