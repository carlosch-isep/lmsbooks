package pt.psoft.g1.psoftg1.sagamanagement.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.psoft.g1.psoftg1.sagamanagement.model.Saga;
import pt.psoft.g1.psoftg1.shared.enums.SagaStatus;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import(SagaService.class)
class SagaServiceIntegrationTest {

    @Autowired
    private SagaService sagaService;

    static class TestData implements Serializable {
        public String name;
        public int value;


        public TestData() {}

        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestData testData = (TestData) o;
            return value == testData.value && name.equals(testData.name);
        }
    }

    @Test
    void testSetAndGetSaga() throws JsonProcessingException, ClassNotFoundException {
        // Arrange
        TestData originalData = new TestData("Integration Test", 100);

        // Act
        Saga savedSaga = sagaService.set(originalData);

        // Assert
        assertNotNull(savedSaga.getSagaId());
        assertEquals(SagaStatus.PENDING, savedSaga.getStatus());
        assertEquals(TestData.class.getName(), savedSaga.getObjectClass());

        // Act
        TestData retrievedData = sagaService.get(savedSaga.getSagaId(), TestData.class);

        // Assert
        assertEquals(originalData, retrievedData);
    }

    @Test
    void testChangeStatus() throws JsonProcessingException {
        // Arrange
        TestData data = new TestData("Status Check", 1);
        Saga savedSaga = sagaService.set(data);

        // Act
        Saga updatedSaga = sagaService.changeStatus(savedSaga.getSagaId(), SagaStatus.APPROVED);

        // Assert
        assertEquals(SagaStatus.APPROVED, updatedSaga.getStatus());

        List<Saga> completedSagas = sagaService.getByStatus(SagaStatus.APPROVED);
        assertTrue(completedSagas.stream().anyMatch(s -> s.getSagaId().equals(savedSaga.getSagaId())));
    }

    @Test
    void testGetWithWrongType() throws JsonProcessingException {
        // Arrange
        TestData data = new TestData("Wrong Type", 50);
        Saga savedSaga = sagaService.set(data);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            sagaService.get(savedSaga.getSagaId(), String.class);
        });

        assertEquals("Expected type is not equal to the type returned by de repo", exception.getMessage());
    }

    @Test
    void testGetNonExistentSaga() {
        // Act & Assert
        UUID randomId = UUID.randomUUID();
        Exception exception = assertThrows(RuntimeException.class, () -> {
            sagaService.get(randomId, TestData.class);
        });

        assertEquals("Saga not found", exception.getMessage());
    }

    @Test
    void testGetByObjectClassAndStatus() throws JsonProcessingException {
        // Arrange
        sagaService.set(new TestData("A", 1));

        Saga s2 = sagaService.set(new TestData("B", 2));
        sagaService.changeStatus(s2.getSagaId(), SagaStatus.APPROVED);

        sagaService.set("String Data");

        // Act
        List<Saga> result = sagaService.getByObjectClassAndStatus(TestData.class.getName(), SagaStatus.PENDING);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(TestData.class.getName(), result.getFirst().getObjectClass());
        assertEquals(SagaStatus.PENDING, result.getFirst().getStatus());
    }
}