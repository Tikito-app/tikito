package org.tikito.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SetUserPreferenceRequest {
    private String key;
    private String value;
}
