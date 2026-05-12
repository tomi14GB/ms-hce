package com.mediflow.hce.strategy;

import com.mediflow.hce.model.ClinicalRecord;
import java.util.List;

/**
 * ============================================================
 * PATRÓN DE DISEÑO: Strategy Pattern
 * ============================================================
 * Define una familia de algoritmos de búsqueda de registros clínicos,
 * intercambiables en tiempo de ejecución según el contexto.
 *
 * Problema que resuelve en MediFlow:
 * - Diferentes tipos de consulta requieren diferentes estrategias:
 *   un médico de urgencia necesita búsqueda por fecha reciente,
 *   un especialista necesita búsqueda por tipo de documento,
 *   un auditor necesita búsqueda por palabra clave.
 * - Sin Strategy, la lógica de búsqueda estaría en un único método
 *   gigante con múltiples condicionales if/else.
 * - Con Strategy, cada algoritmo está encapsulado en su propia clase,
 *   y el Service selecciona la estrategia según el parámetro de búsqueda.
 *
 * Beneficios:
 * - Cada estrategia se puede testear de forma independiente
 * - Agregar una nueva estrategia no modifica las existentes (Open/Closed)
 * - El cliente (Service) no conoce los detalles de cada algoritmo
 * ============================================================
 */

// Interfaz Strategy: define el contrato para todas las estrategias
public interface SearchStrategy {

    /**
     * Ejecuta la búsqueda de registros clínicos según la estrategia.
     * @param records Lista completa de registros a filtrar
     * @param criteria Criterio de búsqueda
     * @return Lista filtrada de registros
     */
    List<ClinicalRecord> search(List<ClinicalRecord> records, String criteria);

    /**
     * Nombre de la estrategia para logging y selección.
     */
    String getStrategyName();
}
