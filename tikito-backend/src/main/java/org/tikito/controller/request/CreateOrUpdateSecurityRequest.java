package org.tikito.controller.request;

import org.tikito.dto.security.SecurityType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrUpdateSecurityRequest {
    private String id;
    private SecurityType securityType;
    private String currency;
    private String name;
}
