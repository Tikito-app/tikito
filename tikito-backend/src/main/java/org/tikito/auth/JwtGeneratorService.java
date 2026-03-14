package org.tikito.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public final class JwtGeneratorService {
    private static final String apiKeySecretBytes = JwtGeneratorService.generateSecret();

    private static final long JWT_EXPIRATION_TIME = 900_000L * 2000; // 15 min * 100

    public static String generateJwt(final AuthUser userAccount, final List<Scope> scopes) {
        final Key key = new SecretKeySpec(apiKeySecretBytes.getBytes(), SignatureAlgorithm.HS512.getJcaName());

        final JwtBuilder builder = Jwts.builder()
                .setSubject("subject")
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME))
                .setIssuer("info@tikito.org")
                .claim("id", String.valueOf(userAccount.getId()))
                .claim("scopes", scopes.stream().map(Enum::toString).collect(Collectors.joining(",")))
                .claim("email", userAccount.getEmail());

        return builder.signWith(key, SignatureAlgorithm.HS512).compact();
    }

    public static String generateSecret() {
        return Encoders.BASE64.encode(Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded());
    }

    public static Jwt<Header, Claims> unsafeParseJwt(final String jwtString) {
        final String withoutSignature = jwtString.substring(0, jwtString.lastIndexOf('.') + 1);
        return Jwts.parserBuilder()
                .build()
                .parseClaimsJwt(withoutSignature);
    }
}
