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
    private String bio;

    public AuthorMongo() {}
    public AuthorMongo(String id, String name, String bio) {
        this.id = id;
        this.name = name;
        this.bio = bio;
    }
}
