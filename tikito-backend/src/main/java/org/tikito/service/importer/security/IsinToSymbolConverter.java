package org.tikito.service.importer.security;

import org.tikito.exception.ResourceNotFoundException;
import org.tikito.util.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public final class IsinToSymbolConverter {
    public static Optional<String> getSymbol(final String isin, final String token) {
        final String url = "https://finnhub.io/api/v1/search?q=" + isin + "&token=" + token;

        try {
            final String json = HttpUtil.downloadUrl(url);
            final JsonNode jsonNode = new ObjectMapper().reader().readTree(json);
            final JsonNode result = jsonNode.get("result");
            if (!result.isEmpty()) {
                return Optional.of(result.get(0).get("symbol").textValue());
            }
            return Optional.empty();
        } catch (final JsonProcessingException | ResourceNotFoundException e) {
            log.error("Could not fetch isin {}", isin, e);
            return Optional.empty();
        }
    }
}
