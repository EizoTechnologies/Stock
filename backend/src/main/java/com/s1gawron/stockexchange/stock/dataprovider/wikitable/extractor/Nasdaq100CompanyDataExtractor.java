package com.s1gawron.stockexchange.stock.dataprovider.wikitable.extractor;

import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompaniesDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompanyDTO;

import java.util.List;
import java.util.Map;

public class Nasdaq100CompanyDataExtractor extends AbstractCompanyDataExtractor {

    private final List<List<List<String>>> data;

    public Nasdaq100CompanyDataExtractor(final List<List<List<String>>> data) {
        this.data = data;
    }

    @Override public IndexCompaniesDTO extract() {
        final Map<Character, List<IndexCompanyDTO>> companies = data.get(0).stream()
            .skip(1) //skip data definition from array
            .map(o -> new IndexCompanyDTO(o.get(1), o.get(0), o.get(2)))
            .collect(collectByTickerFirstLetter());

        return new IndexCompaniesDTO(getAllElementsCount(companies.values()), companies);
    }
}
