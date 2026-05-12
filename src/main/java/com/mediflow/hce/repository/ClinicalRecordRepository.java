package com.mediflow.hce.repository;

import com.mediflow.hce.model.ClinicalRecord;
import com.mediflow.hce.model.ClinicalRecord.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * PATRÓN DE DISEÑO: Repository Pattern
 * ============================================================
 * Abstrae el acceso a datos del MS-HCE, separando la lógica de
 * persistencia de la lógica de negocio (ClinicalRecordService).
 *
 * Problema que resuelve en MediFlow:
 * - El monolito original tenía queries SQL directas mezcladas con
 *   lógica de negocio, generando 12 segundos de latencia por JOINs
 *   masivos entre tablas compartidas.
 * - Con Repository Pattern + Database per Service, el MS-HCE tiene
 *   su propia BD PostgreSQL con índices optimizados para consultas
 *   clínicas, reduciendo latencia a < 2 segundos.
 * - Si se decide migrar de PostgreSQL a otra BD, solo se modifica
 *   esta capa sin tocar el Service ni el Controller.
 *
 * Beneficios:
 * - Aísla queries SQL → facilita pruebas unitarias con mocks
 * - Permite cambiar la implementación de persistencia sin afectar el servicio
 * - Spring Data JPA genera la implementación automáticamente
 * ============================================================
 */
@Repository
public interface ClinicalRecordRepository extends JpaRepository<ClinicalRecord, Long> {

    /**
     * Busca todos los registros clínicos de un paciente.
     * Ordenados por fecha descendente (más recientes primero).
     */
    List<ClinicalRecord> findByPatientIdOrderByRecordDateDesc(String patientId);

    /**
     * Busca registros de un paciente filtrados por tipo de documento.
     * Usado para filtrar solo HL7, DICOM, PDF, etc.
     */
    List<ClinicalRecord> findByPatientIdAndDocumentType(
            String patientId, String documentType);

    /**
     * Busca registros por estado (ACTIVE, COMPLETED, etc.)
     */
    List<ClinicalRecord> findByPatientIdAndStatus(
            String patientId, RecordStatus status);

    /**
     * Busca registros en un rango de fechas para un paciente.
     * Útil para consultas de historial en períodos específicos.
     */
    @Query("SELECT cr FROM ClinicalRecord cr " +
           "WHERE cr.patientId = :patientId " +
           "AND cr.recordDate BETWEEN :startDate AND :endDate " +
           "ORDER BY cr.recordDate DESC")
    List<ClinicalRecord> findByPatientIdAndDateRange(
            @Param("patientId") String patientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Cuenta registros activos de un paciente (para métricas).
     */
    long countByPatientIdAndStatus(String patientId, RecordStatus status);

    /**
     * Busca registros por descripción (búsqueda parcial, case-insensitive).
     */
    @Query("SELECT cr FROM ClinicalRecord cr " +
           "WHERE cr.patientId = :patientId " +
           "AND LOWER(cr.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY cr.recordDate DESC")
    List<ClinicalRecord> searchByKeyword(
            @Param("patientId") String patientId,
            @Param("keyword") String keyword);
}
