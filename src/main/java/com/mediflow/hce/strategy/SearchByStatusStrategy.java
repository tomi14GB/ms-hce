package com.mediflow.hce.strategy;

import com.mediflow.hce.model.ClinicalRecord;
import com.mediflow.hce.model.ClinicalRecord.RecordStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Estrategia concreta: Búsqueda por estado del registro.
 * Filtra registros ACTIVE, COMPLETED, CANCELLED, etc.
 * Útil para médicos que necesitan ver solo registros activos.
 */
@Component
public class SearchByStatusStrategy implements SearchStrategy {

    @Override
    public List<ClinicalRecord> search(List<ClinicalRecord> records, String criteria) {
        try {
            RecordStatus targetStatus = RecordStatus.valueOf(criteria.toUpperCase());
            return records.stream()
                    .filter(r -> r.getStatus() == targetStatus)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return records; // Si el estado no es válido, retorna todos
        }
    }

    @Override
    public String getStrategyName() {
        return "SEARCH_BY_STATUS";
    }
}
