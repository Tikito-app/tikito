package org.tikito.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.tikito.dto.export.ImportExportSettings;

import java.util.Set;

@Getter
@Setter
public class ExportRequest {
    private ImportExportSettings settings;
    private Set<@NotNull Long> accountIds;
}
