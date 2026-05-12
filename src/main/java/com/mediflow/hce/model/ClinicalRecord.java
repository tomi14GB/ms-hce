package com.mediflow.hce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un registro clínico electrónico.
 * Mapeada a la tabla 'clinical_records' en PostgreSQL.
 *
 * Cada registro pertenece a un paciente y tiene un tipo de documento
 * que determina cómo se procesa (ver Factory Method en ClinicalDocumentFactory).
 */
@Entity
@Table(name = "clinical_records")
public class ClinicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El ID del paciente es obligatorio")
    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Column(name = "document_type", nullable = false)
    private String documentType; // HL7, DICOM, PDF, GENOMIC

    @NotBlank(message = "La descripción es obligatoria")
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @NotNull
    @Column(name = "record_date", nullable = false)
    private LocalDateTime recordDate;

    @Column(name = "practitioner")
    private String practitioner;

    @Column(name = "institution")
    private String institution;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enum de estados del registro
    public enum RecordStatus {
        ACTIVE, COMPLETED, CANCELLED, ENTERED_IN_ERROR
    }

    // Constructor vacío (requerido por JPA)
    public ClinicalRecord() {}

    // Constructor con campos obligatorios
    public ClinicalRecord(String patientId, String documentType,
                          String description, LocalDateTime recordDate) {
        this.patientId = patientId;
        this.documentType = documentType;
        this.description = description;
        this.recordDate = recordDate;
        this.status = RecordStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDateTime recordDate) { this.recordDate = recordDate; }

    public String getPractitioner() { return practitioner; }
    public void setPractitioner(String practitioner) { this.practitioner = practitioner; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public RecordStatus getStatus() { return status; }
    public void setStatus(RecordStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
