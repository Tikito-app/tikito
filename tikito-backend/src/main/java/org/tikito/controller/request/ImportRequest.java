package org.tikito.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.export.ImportExportSettings;
import org.tikito.dto.export.TikitoExportDto;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImportRequest {
    private ImportExportSettings settings;
    private TikitoExportDto data;
}
