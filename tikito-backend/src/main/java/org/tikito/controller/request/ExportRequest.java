package org.tikito.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.export.ImportExportSettings;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ExportRequest {
    private ImportExportSettings settings;
    private Set<Long> accountIds;
}
