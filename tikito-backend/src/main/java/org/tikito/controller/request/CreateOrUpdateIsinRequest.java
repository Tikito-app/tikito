package org.tikito.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreateOrUpdateIsinRequest {
    private String isin;
    private Instant validFrom;
    private Instant validTo;
}
