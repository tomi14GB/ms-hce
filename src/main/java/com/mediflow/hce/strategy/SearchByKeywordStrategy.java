package com.mediflow.hce.strategy;

import com.mediflow.hce.model.ClinicalRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Estrategia concreta: Búsqueda por palabra clave.
 * Busca en la descripción y contenido del registro clínico.
 * Útil para auditores y búsquedas generales.
 */
@Component
public class SearchByKeywordStrategy implements SearchStrategy {

    @Override
    public List<ClinicalRecord> search(List<ClinicalRecord> records, String criteria) {
        String keyword = criteria.toLowerCase();
        return records.stream()
                .filter(r ->
                    r.getDescription().toLowerCase().contains(keyword) ||
                    (r.getContent() != null && r.getContent().toLowerCase().contains(keyword))
                )
                .collect(Collectors.toList());
    }

    @Override
    public String getStrategyName() {
        return "SEARCH_BY_KEYWORD";
    }
}
