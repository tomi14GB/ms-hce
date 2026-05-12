package com.mediflow.hce.factory;

import com.mediflow.hce.model.ClinicalRecord;
import java.time.LocalDateTime;

/**
 * ============================================================
 * PATRÓN DE DISEÑO: Factory Method
 * ============================================================
 * Permite crear diferentes tipos de registros clínicos sin exponer
 * la lógica de instanciación al código cliente (Controller/Service).
 *
 * Problema que resuelve en MediFlow:
 * - MediFlow maneja 4+ tipos de documentos clínicos: HL7, DICOM,
 *   PDF, y genómicos. Cada uno tiene reglas de creación diferentes:
 *   - HL7 necesita validación de estructura HL7 v2.x
 *   - DICOM necesita metadata de imágenes médicas
 *   - PDF necesita extracción de texto
 *   - Genómicos necesitan codificación especial
 * - Sin Factory, la lógica de creación estaría dispersa con
 *   condicionales if/else en el Controller, violando el principio
 *   Open/Closed (abierto para extensión, cerrado para modificación).
 * - Con Factory, agregar un nuevo tipo de documento (ej: FHIR R5)
 *   solo requiere agregar un nuevo case, sin modificar el resto.
 *
 * Beneficios:
 * - Centraliza la lógica de creación en un solo lugar
 * - Facilita agregar nuevos tipos sin modificar código existente
 * - Cada tipo aplica sus validaciones específicas
 * ============================================================
 */
public class ClinicalDocumentFactory {

    /**
     * Crea un registro clínico según el tipo de documento.
     * Cada tipo aplica configuraciones y validaciones específicas.
     *
     * @param type Tipo de documento: HL7, DICOM, PDF, GENOMIC
     * @param patientId ID del paciente
     * @param description Descripción del registro
     * @param content Contenido del documento
     * @param practitioner Médico responsable
     * @param institution Institución de origen
     * @return ClinicalRecord configurado según el tipo
     * @throws IllegalArgumentException si el tipo no es soportado
     */
    public static ClinicalRecord createDocument(
            String type,
            String patientId,
            String description,
            String content,
            String practitioner,
            String institution) {

        ClinicalRecord record = new ClinicalRecord(
                patientId, type, description, LocalDateTime.now()
        );
        record.setPractitioner(practitioner);
        record.setInstitution(institution);

        switch (type.toUpperCase()) {
            case "HL7":
                // Documento HL7 v2.x: mensajes clínicos estandarizados
                record.setContent(validateAndFormatHL7(content));
                record.setDocumentType("HL7");
                break;

            case "DICOM":
                // Documento DICOM: imágenes médicas (radiografías, TAC, etc.)
                record.setContent(processDicomMetadata(content));
                record.setDocumentType("DICOM");
                break;

            case "PDF":
                // Documento PDF: informes, epicrisis, certificados
                record.setContent(extractPdfContent(content));
                record.setDocumentType("PDF");
                break;

            case "GENOMIC":
                // Documento genómico: datos de secuenciación
                record.setContent(encodeGenomicData(content));
                record.setDocumentType("GENOMIC");
                break;

            default:
                throw new IllegalArgumentException(
                    "Tipo de documento no soportado: " + type +
                    ". Tipos válidos: HL7, DICOM, PDF, GENOMIC"
                );
        }

        return record;
    }

    // --- Métodos privados de procesamiento por tipo ---

    private static String validateAndFormatHL7(String content) {
        // Valida estructura básica HL7 v2.x (segmentos MSH, PID, etc.)
        if (content == null || content.isEmpty()) {
            return "MSH|^~\\&|MEDIFLOW|HCE|||";
        }
        // En producción: validar segmentos HL7, parsear con HAPI HL7
        return content.trim();
    }

    private static String processDicomMetadata(String content) {
        // Extrae metadata DICOM (Patient Name, Study Date, Modality)
        if (content == null || content.isEmpty()) {
            return "{\"modality\":\"UNKNOWN\",\"studyDate\":\"\"}";
        }
        // En producción: parsear con dcm4che, extraer tags DICOM
        return content.trim();
    }

    private static String extractPdfContent(String content) {
        // Procesa contenido PDF (extracción de texto, OCR si es escaneado)
        if (content == null || content.isEmpty()) {
            return "[Documento PDF sin contenido extractable]";
        }
        // En producción: usar Apache PDFBox o Tika para extracción
        return content.trim();
    }

    private static String encodeGenomicData(String content) {
        // Codifica datos genómicos (formato VCF simplificado)
        if (content == null || content.isEmpty()) {
            return "{\"format\":\"VCF\",\"variants\":[]}";
        }
        // En producción: validar formato VCF, FASTQ, etc.
        return content.trim();
    }
}
