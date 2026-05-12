package com.mediflow.hce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de Historial Clínico Electrónico (MS-HCE).
 *
 * Responsabilidades:
 * - CRUD de registros clínicos de pacientes
 * - Gestión de diferentes tipos de documentos (DICOM, HL7, PDF, genómicos)
 * - Exposición de endpoints REST compatibles con FHIR R4
 *
 * Base de datos propia: PostgreSQL (patrón Database per Service)
 * Puerto: 8081
 */
@SpringBootApplication
public class MsHceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsHceApplication.class, args);
    }
}
