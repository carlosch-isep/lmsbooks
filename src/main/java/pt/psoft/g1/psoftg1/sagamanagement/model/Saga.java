package pt.psoft.g1.psoftg1.sagamanagement.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.psoft.g1.psoftg1.shared.enums.SagaStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Saga {
    private UUID sagaId;

    private SagaStatus status;

    private String objectClass;

    private String objectData;

    private boolean sagaDependencies;

    private Instant createdAt;

    private Instant updatedAt;

    public Saga(SagaStatus status, String objectClass, String objectData){
        this.status = status;
        this.objectClass = objectClass;
        this.objectData = objectData;
        validation();
    }

    public Saga(UUID exists, SagaStatus sagaStatus, String objectClass, String objectData) {
        this.sagaId = exists;
        this.status = sagaStatus;
        this.objectClass = objectClass;
        this.objectData = objectData;

        validation();
    }

    private void validation(){
        if(this.status == null || this.objectClass == null || this.objectData == null){
            throw new IllegalArgumentException("status, objectClass  and objectData cannot be nullc");
        }
    }
}

