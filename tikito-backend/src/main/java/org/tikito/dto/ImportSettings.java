package org.tikito.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImportSettings {
    private boolean canImportNewIsin;
    private boolean canImportNewCurrency;
}
