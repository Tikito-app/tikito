package org.tikito.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public final class JwtGeneratorService {
//    private static final String apiKeySecretBytes = JwtGeneratorService.generateSecret();

    private static final long JWT_EXPIRATION_TIME = 900_000L * 2000; // 15 min * 100

    public static String generateJwt(final String apiKey, final AuthUser userAccount, final List<Scope> scopes) {
        final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(apiKey));

        return Jwts.builder()
                .subject("subject")
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME))
                .issuer("info@tikito.org")
                .claim("id", String.valueOf(userAccount.getId()))
                .claim("scopes", scopes.stream().map(Enum::toString).collect(Collectors.joining(",")))
                .claim("email", userAccount.getEmail())
                .signWith(key)
                .compact();
    }

    public static String generateSecret() {
        return Encoders.BASE64.encode(Jwts.SIG.HS512.key().build().getEncoded());
    }

    public static Jws<Claims> unsafeParseJwt(final String apiKey, final String jwtString) {
        final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(apiKey));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwtString);
    }
}
