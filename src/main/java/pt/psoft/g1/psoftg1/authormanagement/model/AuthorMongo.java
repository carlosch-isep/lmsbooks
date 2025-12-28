package pt.psoft.g1.psoftg1.authormanagement.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;
import java.io.Serializable;

@Getter
@Setter
@Profile("command")
public class AuthorMongo implements Serializable {
    private String id;
    private String name;

    public AuthorMongo() {}
    public AuthorMongo(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
