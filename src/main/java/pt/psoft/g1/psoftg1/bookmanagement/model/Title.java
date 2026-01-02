package pt.psoft.g1.psoftg1.bookmanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Embeddable
@Getter
public class Title {
    @NotBlank(message = "Title cannot be blank")
    @Size(min = 1)
    @Column(name = "TITLE")
    String title;

    protected Title() {
    }

    public Title(String title) {
        setTitle(title);
    }

    public void setTitle(String title) {
        if (title == null)
            throw new IllegalArgumentException("Title cannot be null");
        if (title.isBlank())
            throw new IllegalArgumentException("Title cannot be blank");
        if (title.length() > 100)
            throw new IllegalArgumentException("Title has a maximum of 100 characters");
        this.title = title.strip();
    }

    public String toString() {
        return this.title;
    }
}
