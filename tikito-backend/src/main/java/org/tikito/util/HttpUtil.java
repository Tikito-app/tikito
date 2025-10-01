package org.tikito.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.tikito.exception.ResourceNotFoundException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Slf4j
public final class HttpUtil {
    private static HttpClient httpClient;

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
        final HttpResponse response;
        final HttpGet get = new HttpGet(url);
        try {
            response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() == 404) {
                throw new ResourceNotFoundException(url);
            }
            return EntityUtils.toString(response.getEntity());
        } catch (final IOException e) {
            log.error("Error", e);
            return null;
        } finally {
            get.releaseConnection();
        }
    }

    public static byte[] downloadUrlAsBytes(final String url) throws IOException, ResourceNotFoundException {
        if (httpClient == null) {
            httpClient = createTrustAllHttpClientBuilder().build();
        }
        final HttpResponse response;
        log.info("Downloading as bytes {}", url);
        response = httpClient.execute(new HttpGet(url));
        if (response.getStatusLine().getStatusCode() == 404) {
            throw new ResourceNotFoundException(url);
        }
        return EntityUtils.toByteArray(response.getEntity());
    }

    public static HttpClientBuilder createTrustAllHttpClientBuilder() {
        final SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, (chain, authType) -> true);
        } catch (final NoSuchAlgorithmException | KeyStoreException e) {
            log.error(e.getMessage(), e);
        }
        SSLConnectionSocketFactory sslsf = null;
        try {
            sslsf = new
                    SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
        } catch (final NoSuchAlgorithmException | KeyManagementException e) {
            log.error(e.getMessage(), e);
        }
        final int timeout = 5000;
        final RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder.setConnectTimeout(timeout);
        requestBuilder.setConnectionRequestTimeout(timeout);
        requestBuilder.setSocketTimeout(timeout);

        final HttpClientBuilder builder1 = HttpClients.custom().setSSLSocketFactory(sslsf);
        builder1.setDefaultRequestConfig(requestBuilder.build());
        builder1.setUserAgent("Mozilla/5.0 Firefox/" + randomInt(25, 50) + ".0");
        return builder1;
    }

    public static int randomInt(final int min, final int max) {
        final Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}
