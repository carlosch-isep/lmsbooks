package pt.psoft.g1.psoftg1.sagamanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.sagamanagement.entities.SagaEntity;
import pt.psoft.g1.psoftg1.shared.enums.SagaStatus;

import java.util.List;

@Repository
public interface SagaRepository extends JpaRepository<SagaEntity, String> {
    List<SagaEntity> findByStatus(SagaStatus status);
    List<SagaEntity> findByObjectClassAndStatus(String objectClass, SagaStatus status);
}
