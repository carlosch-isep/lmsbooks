package pt.psoft.g1.psoftg1.sagamanagement.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.psoft.g1.psoftg1.sagamanagement.model.Saga;
import pt.psoft.g1.psoftg1.shared.enums.SagaStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saga_instances")
@Getter
@Setter
@NoArgsConstructor
public class SagaEntity {

    @Id
    @Column(name = "saga_id")
    private String sagaId; // No SQLite, armazenar como String é mais seguro para IDs

    @Enumerated(EnumType.STRING)
    private SagaStatus status;

    private String objectClass;

    @Column(columnDefinition = "TEXT")
    private String objectData;

    private boolean sagaDependencies;

    private Instant createdAt;

    private Instant updatedAt;

    // Converte Domínio -> Entidade
    public SagaEntity(Saga saga) {
        this.sagaId = saga.getSagaId() != null ? saga.getSagaId().toString() : UUID.randomUUID().toString();
        this.status = saga.getStatus();
        this.objectClass = saga.getObjectClass();
        this.objectData = saga.getObjectData();
        this.sagaDependencies = saga.isSagaDependencies();
        this.createdAt = saga.getCreatedAt() != null ? saga.getCreatedAt() : Instant.now();
        this.updatedAt = Instant.now();
    }

    public Saga toDomain() {
        return new Saga(
                UUID.fromString(this.sagaId),
                this.status,
                this.objectClass,
                this.objectData,
                this.sagaDependencies,
                this.createdAt,
                this.updatedAt
        );
    }
}