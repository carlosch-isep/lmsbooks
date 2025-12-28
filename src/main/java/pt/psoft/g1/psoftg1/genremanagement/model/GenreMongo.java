package pt.psoft.g1.psoftg1.genremanagement.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;

@Getter
@Setter
@Profile("mongodb")
public class GenreMongo implements Serializable {
    private String id;
    private String name;

    public GenreMongo(String id, String name) {
        this.name = name;
        this.id = id;
    }

    public GenreMongo() {

    }
}






