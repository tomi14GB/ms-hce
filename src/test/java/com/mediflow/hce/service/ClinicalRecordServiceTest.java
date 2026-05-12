package com.mediflow.hce.service;

import com.mediflow.hce.factory.ClinicalDocumentFactory;
import com.mediflow.hce.model.ClinicalRecord;
import com.mediflow.hce.model.ClinicalRecord.RecordStatus;
import com.mediflow.hce.repository.ClinicalRecordRepository;
import com.mediflow.hce.strategy.SearchByTypeStrategy;
import com.mediflow.hce.strategy.SearchByKeywordStrategy;
import com.mediflow.hce.strategy.SearchByStatusStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del MS-HCE.
 * Usan Mockito para simular el Repository (no necesitan BD real).
 * Verifican la correcta aplicación de los patrones de diseño.
 */
@ExtendWith(MockitoExtension.class)
class ClinicalRecordServiceTest {

    @Mock
    private ClinicalRecordRepository repository;

    private ClinicalRecordService service;

    private ClinicalRecord sampleRecord;
    private List<ClinicalRecord> sampleRecords;

    @BeforeEach
    void setUp() {
        // Inyecta las estrategias reales y el repositorio mock
        service = new ClinicalRecordService(
                repository,
                new SearchByTypeStrategy(),
                new SearchByKeywordStrategy(),
                new SearchByStatusStrategy()
        );

        // Datos de prueba
        sampleRecord = new ClinicalRecord(
                "PAT-001", "HL7", "Consulta general",
                LocalDateTime.of(2025, 1, 15, 10, 0)
        );
        sampleRecord.setId(1L);
        sampleRecord.setPractitioner("Dr. García");
        sampleRecord.setInstitution("Hospital Central");
        sampleRecord.setContent("MSH|^~\\&|MEDIFLOW|HCE|||");

        ClinicalRecord record2 = new ClinicalRecord(
                "PAT-001", "DICOM", "Radiografía tórax",
                LocalDateTime.of(2025, 2, 10, 14, 30)
        );
        record2.setId(2L);
        record2.setStatus(RecordStatus.COMPLETED);

        ClinicalRecord record3 = new ClinicalRecord(
                "PAT-001", "PDF", "Epicrisis hospitalaria",
                LocalDateTime.of(2025, 3, 5, 9, 0)
        );
        record3.setId(3L);

        sampleRecords = Arrays.asList(sampleRecord, record2, record3);
    }

    // ===== Tests del Repository Pattern =====

    @Test
    @DisplayName("Repository: obtiene registros por paciente")
    void getRecordsByPatient_returnsRecords() {
        when(repository.findByPatientIdOrderByRecordDateDesc("PAT-001"))
                .thenReturn(sampleRecords);

        List<ClinicalRecord> result = service.getRecordsByPatient("PAT-001");

        assertEquals(3, result.size());
        verify(repository).findByPatientIdOrderByRecordDateDesc("PAT-001");
    }

    @Test
    @DisplayName("Repository: obtiene registro por ID")
    void getRecordById_found() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleRecord));

        Optional<ClinicalRecord> result = service.getRecordById(1L);

        assertTrue(result.isPresent());
        assertEquals("PAT-001", result.get().getPatientId());
    }

    @Test
    @DisplayName("Repository: retorna vacío si no existe")
    void getRecordById_notFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<ClinicalRecord> result = service.getRecordById(999L);

        assertFalse(result.isPresent());
    }

    // ===== Tests del Factory Method =====

    @Test
    @DisplayName("Factory: crea documento HL7 correctamente")
    void createRecord_HL7_success() {
        when(repository.save(any(ClinicalRecord.class)))
                .thenAnswer(inv -> {
                    ClinicalRecord r = inv.getArgument(0);
                    r.setId(10L);
                    return r;
                });

        ClinicalRecord result = service.createRecord(
                "PAT-002", "HL7", "Ingreso urgencia",
                "MSH|^~\\&|MEDIFLOW|", "Dr. López", "Clínica Sur"
        );

        assertNotNull(result);
        assertEquals("HL7", result.getDocumentType());
        assertEquals("PAT-002", result.getPatientId());
        verify(repository).save(any(ClinicalRecord.class));
    }

    @Test
    @DisplayName("Factory: crea documento DICOM correctamente")
    void createRecord_DICOM_success() {
        when(repository.save(any(ClinicalRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ClinicalRecord result = service.createRecord(
                "PAT-003", "DICOM", "TAC cerebral",
                "{\"modality\":\"CT\"}", "Dr. Muñoz", "Hospital Norte"
        );

        assertEquals("DICOM", result.getDocumentType());
        assertEquals("TAC cerebral", result.getDescription());
    }

    @Test
    @DisplayName("Factory: lanza excepción con tipo inválido")
    void createRecord_invalidType_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createRecord(
                        "PAT-001", "INVALIDO", "Test", "content",
                        "Dr. Test", "Hospital Test"
                )
        );
    }

    // ===== Tests del Factory Method directo =====

    @Test
    @DisplayName("Factory: cada tipo genera documentType correcto")
    void factory_allTypes_setCorrectDocumentType() {
        String[] types = {"HL7", "DICOM", "PDF", "GENOMIC"};
        for (String type : types) {
            ClinicalRecord record = ClinicalDocumentFactory.createDocument(
                    type, "PAT-X", "desc", "content", "Dr.", "Hosp."
            );
            assertEquals(type, record.getDocumentType());
        }
    }

    // ===== Tests del Strategy Pattern =====

    @Test
    @DisplayName("Strategy: búsqueda por tipo filtra correctamente")
    void searchRecords_byType_filtersCorrectly() {
        when(repository.findByPatientIdOrderByRecordDateDesc("PAT-001"))
                .thenReturn(sampleRecords);

        List<ClinicalRecord> results = service.searchRecords(
                "PAT-001", "type", "HL7");

        assertEquals(1, results.size());
        assertEquals("HL7", results.get(0).getDocumentType());
    }

    @Test
    @DisplayName("Strategy: búsqueda por keyword filtra en descripción")
    void searchRecords_byKeyword_filtersCorrectly() {
        when(repository.findByPatientIdOrderByRecordDateDesc("PAT-001"))
                .thenReturn(sampleRecords);

        List<ClinicalRecord> results = service.searchRecords(
                "PAT-001", "keyword", "radiografía");

        assertEquals(1, results.size());
        assertEquals("Radiografía tórax", results.get(0).getDescription());
    }

    @Test
    @DisplayName("Strategy: búsqueda por status filtra correctamente")
    void searchRecords_byStatus_filtersCorrectly() {
        when(repository.findByPatientIdOrderByRecordDateDesc("PAT-001"))
                .thenReturn(sampleRecords);

        List<ClinicalRecord> results = service.searchRecords(
                "PAT-001", "status", "COMPLETED");

        assertEquals(1, results.size());
        assertEquals(RecordStatus.COMPLETED, results.get(0).getStatus());
    }

    @Test
    @DisplayName("Strategy: modo inválido lanza excepción")
    void searchRecords_invalidMode_throwsException() {
        when(repository.findByPatientIdOrderByRecordDateDesc("PAT-001"))
                .thenReturn(sampleRecords);

        assertThrows(IllegalArgumentException.class, () ->
                service.searchRecords("PAT-001", "invalido", "test")
        );
    }

    // ===== Test de actualización de estado =====

    @Test
    @DisplayName("Actualiza estado de registro correctamente")
    void updateStatus_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleRecord));
        when(repository.save(any(ClinicalRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Optional<ClinicalRecord> result = service.updateStatus(
                1L, RecordStatus.COMPLETED);

        assertTrue(result.isPresent());
        assertEquals(RecordStatus.COMPLETED, result.get().getStatus());
    }
}
