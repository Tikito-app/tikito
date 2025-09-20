package org.tikito.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthUser {
    private String jwt;
    private long id;
    private String email;
    private Set<Scope> scopes;
}
