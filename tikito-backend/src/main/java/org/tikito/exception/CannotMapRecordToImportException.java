package org.tikito.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CannotMapRecordToImportException extends Exception {
    private String reason;
    private int lineNumber;
    private String[] line;

    public CannotMapRecordToImportException(final String reason, final int lineNumber, final String[] line) {
        this.reason = reason;
        this.lineNumber = lineNumber;
        this.line = line;
    }
}
