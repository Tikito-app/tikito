package org.tikito.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AuthorizationFilter extends GenericFilterBean {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String INTERNAL_USER_AUTHORIZATION_PREFIX = "Bearer ";

    private final String apiKey;

    public AuthorizationFilter(final String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest instanceof final HttpServletRequest httpServletRequest)) {
            return;
        }

        final String authorizationHeader = httpServletRequest.getHeader(AUTHORIZATION_HEADER);
        AuthUser user = null;
        String jwt = null;


        if (StringUtils.hasLength(authorizationHeader)) {
            jwt = authorizationHeader.replace(INTERNAL_USER_AUTHORIZATION_PREFIX, "");
        } else {
            if (httpServletRequest.getCookies() != null) {
                jwt = Arrays.stream(httpServletRequest.getCookies())
                        .filter(c -> c.getName().equals("jwt"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .orElse(null);
            }
        }
        try {

            if (StringUtils.hasText(jwt)) {
                final Jws<Claims> jws = JwtGeneratorService.unsafeParseJwt(apiKey, jwt);

                Set<Scope> scopes = new HashSet<>();
                try {
                    scopes = Arrays.stream(jws.getPayload()
                            .get("scopes")
                            .toString()
                            .split(","))
                            .filter(StringUtils::hasText)
                            .map(Scope::valueOf)
                            .collect(Collectors.toSet());
                } catch (final Exception e) {
                    log.error("Error parsing scopes from JWT", e);
                }

                user = parseUser(jwt, scopes);
            }

            if (user != null) {
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>()));
            }
        } catch (final ExpiredJwtException e) {
            log.warn("JWT expired", e);
        } catch (final Exception e) {
            log.error("JWT validation failed", e);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    AuthUser parseUser(final String jwt, final Set<Scope> scopes) {
        final Jws<Claims> jws = JwtGeneratorService.unsafeParseJwt(apiKey, jwt);
        return new AuthUser(
                jwt,
                Long.parseLong(getClaim("id", jws.getPayload())),
                getClaim("email", jws.getPayload()),
                scopes);
    }

    private String getClaim(final String name, final Claims claims) {
        final Object o = claims.get(name);
        if (o != null) {
            return o.toString();
        }
        return null;
    }
}
