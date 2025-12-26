package pt.psoft.g1.psoftg1.sagamanagement.model;

public interface SagaDependencies {

    /**
     * All Saga dependencies must have this method, this will be used in SagaServices
     * @return true if all dependencies are resolved
     */
    boolean areDependenciesResolved();
}
