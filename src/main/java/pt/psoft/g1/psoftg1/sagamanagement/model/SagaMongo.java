package pt.psoft.g1.psoftg1.sagamanagement.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.io.Serializable;

@Getter
@Setter
@Profile("command")
@Document(collection = "saga")
public class SagaMongo implements Serializable {
    @Id
    private String id;
    private String sagaType;
    private String status;
    private String objectClass;
    private String objectId;
    private String payload;
    private Long version;

    public SagaMongo() {}

    public SagaMongo(String id, String sagaType, String status, String objectClass, String objectId, String payload, Long version) {
        this.id = id;
        this.sagaType = sagaType;
        this.status = status;
        this.objectClass = objectClass;
        this.objectId = objectId;
        this.payload = payload;
        this.version = version;
    }
}
