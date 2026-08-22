package org.tikito.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.tikito.exception.ResourceNotFoundException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Slf4j
public final class HttpUtil {
    private static CloseableHttpClient httpClient;

    private HttpUtil() {
    }

    /**
     * Don't use this method when non-text (like pdf) is expected.
     */
    public static String downloadUrl(final String url) throws ResourceNotFoundException {
        log.info("Downloading {}", url);
        if (httpClient == null) {
            httpClient = createTrustAllHttpClientBuilder().build();
        }
        final HttpGet get = new HttpGet(url);
        try (final ClassicHttpResponse response = httpClient.executeOpen(null, get, null)) {
            if (response.getCode() == 404) {
                throw new ResourceNotFoundException(url);
            }
            return EntityUtils.toString(response.getEntity());
        } catch (final IOException | ParseException e) {
            log.error("Error", e);
            return null;
        }
    }

    public static HttpClientBuilder createTrustAllHttpClientBuilder() {
        final SSLContextBuilder builder = SSLContextBuilder.create();
        try {
            builder.loadTrustMaterial(null, (_, _) -> true);
        } catch (final NoSuchAlgorithmException | KeyStoreException e) {
            log.error(e.getMessage(), e);
        }
        TlsSocketStrategy tlsSocketStrategy = null;
        try {
            final SSLContext sslContext = builder.build();
            tlsSocketStrategy = new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
        } catch (final NoSuchAlgorithmException | KeyManagementException e) {
            log.error(e.getMessage(), e);
        }
        final Timeout timeout = Timeout.ofMilliseconds(5000);
        final RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder.setConnectionRequestTimeout(timeout);
        requestBuilder.setResponseTimeout(timeout);

        final PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsSocketStrategy)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(timeout)
                        .build())
                .build();

        final HttpClientBuilder builder1 = HttpClients.custom().setConnectionManager(connectionManager);
        builder1.setDefaultRequestConfig(requestBuilder.build());
        builder1.setUserAgent("Mozilla/5.0 Firefox/" + randomInt(25, 50) + ".0");
        return builder1;
    }

    public static int randomInt(final int min, final int max) {
        final Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}
