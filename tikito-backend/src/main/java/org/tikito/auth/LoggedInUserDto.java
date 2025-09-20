package org.tikito.auth;

import org.tikito.entity.UserAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoggedInUserDto {
    private long id;
    private String email;
    private String jwt;

    public LoggedInUserDto(final UserAccount userAccount, final String jwt) {
        this.id = userAccount.getId();
        this.email = userAccount.getEmail();
        this.jwt = jwt;
    }
}
