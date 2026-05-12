package com.mediflow.hce.strategy;

import com.mediflow.hce.model.ClinicalRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Estrategia concreta: Búsqueda por tipo de documento.
 * Filtra registros por HL7, DICOM, PDF o GENOMIC.
 */
@Component
public class SearchByTypeStrategy implements SearchStrategy {

    @Override
    public List<ClinicalRecord> search(List<ClinicalRecord> records, String criteria) {
        return records.stream()
                .filter(r -> r.getDocumentType().equalsIgnoreCase(criteria))
                .collect(Collectors.toList());
    }

    @Override
    public String getStrategyName() {
        return "SEARCH_BY_TYPE";
    }
}
