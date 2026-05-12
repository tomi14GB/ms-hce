package com.mediflow.hce.controller;

import com.mediflow.hce.model.ClinicalRecord;
import com.mediflow.hce.service.ClinicalRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST del MS-HCE.
 * Expone endpoints para gestión de registros clínicos.
 * Base path: /api/v1/clinical-records
 */
@RestController
@RequestMapping("/api/v1/clinical-records")
@CrossOrigin(origins = "*")
public class ClinicalRecordController {

    private final ClinicalRecordService service;

    public ClinicalRecordController(ClinicalRecordService service) {
        this.service = service;
    }

    /**
     * GET /api/v1/clinical-records/patient/{patientId}
     * Obtiene todos los registros clínicos de un paciente.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ClinicalRecord>> getByPatient(
            @PathVariable String patientId) {
        List<ClinicalRecord> records = service.getRecordsByPatient(patientId);
        return ResponseEntity.ok(records);
    }

    /**
     * GET /api/v1/clinical-records/{id}
     * Obtiene un registro clínico por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClinicalRecord> getById(@PathVariable Long id) {
        return service.getRecordById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/clinical-records
     * Crea un nuevo registro clínico usando Factory Method.
     */
    @PostMapping
    public ResponseEntity<ClinicalRecord> create(
            @RequestBody Map<String, String> request) {
        ClinicalRecord record = service.createRecord(
                request.get("patientId"),
                request.get("documentType"),
                request.get("description"),
                request.get("content"),
                request.get("practitioner"),
                request.get("institution")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    /**
     * GET /api/v1/clinical-records/patient/{patientId}/search
     * Busca registros usando Strategy Pattern.
     * Params: mode (type|keyword|status), criteria (valor a buscar)
     */
    @GetMapping("/patient/{patientId}/search")
    public ResponseEntity<List<ClinicalRecord>> search(
            @PathVariable String patientId,
            @RequestParam String mode,
            @RequestParam String criteria) {
        List<ClinicalRecord> results = service.searchRecords(
                patientId, mode, criteria);
        return ResponseEntity.ok(results);
    }

    /**
     * PATCH /api/v1/clinical-records/{id}/status
     * Actualiza el estado de un registro.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ClinicalRecord> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        ClinicalRecord.RecordStatus status = ClinicalRecord.RecordStatus
                .valueOf(request.get("status").toUpperCase());
        return service.updateStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
