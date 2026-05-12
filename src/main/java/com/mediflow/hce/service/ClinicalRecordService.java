package com.mediflow.hce.service;

import com.mediflow.hce.factory.ClinicalDocumentFactory;
import com.mediflow.hce.model.ClinicalRecord;
import com.mediflow.hce.repository.ClinicalRecordRepository;
import com.mediflow.hce.strategy.SearchStrategy;
import com.mediflow.hce.strategy.SearchByTypeStrategy;
import com.mediflow.hce.strategy.SearchByKeywordStrategy;
import com.mediflow.hce.strategy.SearchByStatusStrategy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Capa de servicio del MS-HCE.
 * Orquesta la lógica de negocio utilizando tres patrones:
 *
 * 1. Repository Pattern → acceso a datos via ClinicalRecordRepository
 * 2. Factory Method → creación de registros via ClinicalDocumentFactory
 * 3. Strategy Pattern → búsqueda dinámica via SearchStrategy
 */
@Service
public class ClinicalRecordService {

    private final ClinicalRecordRepository repository;
    private final Map<String, SearchStrategy> strategies;

    /**
     * Constructor con inyección de dependencias.
     * Registra las estrategias de búsqueda disponibles.
     */
    public ClinicalRecordService(
            ClinicalRecordRepository repository,
            SearchByTypeStrategy searchByType,
            SearchByKeywordStrategy searchByKeyword,
            SearchByStatusStrategy searchByStatus) {

        this.repository = repository;

        // Registro de estrategias (Strategy Pattern - Context)
        this.strategies = new HashMap<>();
        this.strategies.put("type", searchByType);
        this.strategies.put("keyword", searchByKeyword);
        this.strategies.put("status", searchByStatus);
    }

    /**
     * Obtiene todos los registros clínicos de un paciente.
     * Usa el Repository Pattern para abstraer el acceso a PostgreSQL.
     */
    public List<ClinicalRecord> getRecordsByPatient(String patientId) {
        return repository.findByPatientIdOrderByRecordDateDesc(patientId);
    }

    /**
     * Obtiene un registro por su ID.
     */
    public Optional<ClinicalRecord> getRecordById(Long id) {
        return repository.findById(id);
    }

    /**
     * Crea un nuevo registro clínico.
     * Usa Factory Method para instanciar según el tipo de documento.
     */
    public ClinicalRecord createRecord(String patientId, String documentType,
                                        String description, String content,
                                        String practitioner, String institution) {
        // Factory Method: crea el registro con las validaciones del tipo
        ClinicalRecord record = ClinicalDocumentFactory.createDocument(
                documentType, patientId, description, content,
                practitioner, institution
        );

        // Repository Pattern: persiste en PostgreSQL
        return repository.save(record);
    }

    /**
     * Busca registros usando Strategy Pattern.
     * El parámetro 'searchMode' determina qué estrategia usar.
     *
     * @param patientId ID del paciente
     * @param searchMode Modo de búsqueda: "type", "keyword", "status"
     * @param criteria Valor a buscar
     * @return Lista filtrada de registros
     */
    public List<ClinicalRecord> searchRecords(String patientId,
                                               String searchMode,
                                               String criteria) {
        // Obtiene todos los registros del paciente
        List<ClinicalRecord> allRecords = repository
                .findByPatientIdOrderByRecordDateDesc(patientId);

        // Selecciona la estrategia según el modo (Strategy Pattern)
        SearchStrategy strategy = strategies.get(searchMode.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException(
                "Modo de búsqueda no válido: " + searchMode +
                ". Modos disponibles: type, keyword, status"
            );
        }

        // Ejecuta la estrategia seleccionada
        return strategy.search(allRecords, criteria);
    }

    /**
     * Actualiza el estado de un registro clínico.
     */
    public Optional<ClinicalRecord> updateStatus(Long id,
                                                  ClinicalRecord.RecordStatus newStatus) {
        return repository.findById(id).map(record -> {
            record.setStatus(newStatus);
            return repository.save(record);
        });
    }

    /**
     * Cuenta registros activos de un paciente (métricas).
     */
    public long countActiveRecords(String patientId) {
        return repository.countByPatientIdAndStatus(
                patientId, ClinicalRecord.RecordStatus.ACTIVE);
    }
}
