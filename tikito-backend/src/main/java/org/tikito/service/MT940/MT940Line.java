package org.tikito.service.MT940;

import lombok.Getter;

@Getter
public abstract class MT940Line {
    protected final String line;

    public MT940Line(final String line) {
        this.line = line;
    }
}
