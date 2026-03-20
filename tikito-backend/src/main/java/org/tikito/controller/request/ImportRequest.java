package org.tikito.controller.request;

import lombok.Getter;
import lombok.Setter;
import org.tikito.dto.export.ImportExportSettings;
import org.tikito.dto.export.TikitoExportDto;

@Getter
@Setter
public class ImportRequest {
    private ImportExportSettings settings;
    private TikitoExportDto data;
}
