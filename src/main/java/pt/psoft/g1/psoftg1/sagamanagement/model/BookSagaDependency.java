package pt.psoft.g1.psoftg1.sagamanagement.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.authormanagement.services.AuthorService;
import pt.psoft.g1.psoftg1.authormanagement.services.CreateAuthorRequest;
import pt.psoft.g1.psoftg1.bookmanagement.services.CreateBookRequest;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;

import java.util.List;

@Component
public class BookSagaDependency implements SagaDependencies {

    /**
     * Variable to save genre status dependency (true, if dependency is resolved)
     */
    private boolean genre;

    /**
     * Variable to save authors status dependency (true, if dependency is resolved)
     */
    private boolean authors;

    @Autowired
    private GenreService genreService;

    @Autowired
    private AuthorService authorService;


    /**
     * Create book saga operations
     * @param request Create book request
     */
    public void setRequest(CreateBookRequest request){
        this.genre = this.checkGenre(request.getGenre());
        this.authors = this.checkAuthors(request.getAuthors());
    }

    /**
     * Check if all dependencies are resolved
     * @return true if all dependencies are resolved
     */
    public boolean areDependenciesResolved(){
        return this.genre && this.authors;
    }

    /**
     * Check if genre are created
     * @return Check if genre exists
     */
    private boolean checkGenre(String genre){
        return genreService.findByString(genre).isPresent();
    }

    /**
     * Check if all authors are created
     * @return Check if all authors exists
     */
    private boolean checkAuthors(List<CreateAuthorRequest> authors){
        for(CreateAuthorRequest authorRequest : authors ){
            if(authorService.findByName(authorRequest.getName()).isEmpty()){
                return false;
            }
        }
        return true;
    }

}

