package com.infernokun.config.feign;

import feign.*;
import lombok.Getter;
import lombok.Setter;
import okhttp3.OkHttpClient;
import okhttp3.tls.HandshakeCertificates;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

@Configuration
@Getter
@Setter
public class InfernoCTFFeignClientConfig {
    /*@Value("${ssl.truststore.uri}")
    private String trustStore = null;
    @Value("${ssl.truststore.password}")
    private String trustStorePassword = null;
    @Value("${ssl.keystore.uri}")
    private String keyStore = null;
    @Value("${ssl.keystore.password}")
    private String keyStorePassword = null;

    private final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(InfernoCTFFeignClientConfig.class);
    private static final ThreadLocal<Map<String, List<String>>> cookies = new ThreadLocal<>();

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    RequestInterceptor bearerAuthRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Accept", MediaType.APPLICATION_JSON_VALUE);
        };
    }

    @Bean
    @Profile({"http", "https"})
    public Client sslClient() {
        return new feign.okhttp.OkHttpClient(
                new OkHttpClient.Builder()
                        .sslSocketFactory(getSSLSocketFactory(), x509TrustManager())
                        .hostnameVerifier(new NoopHostnameVerifier())
                        .build()
        );
    }

    @Bean Logger feignLogger() {
        return new Logger() {
            @Override
            protected void log(String configKey, String format, Object... args) {
                LOGGER.info(String.format(methodTag(configKey) + format, args));
            }

            @Override
            protected void logRequest(String configKey, Level logLevel, Request request) {
                super.logRequest(configKey, logLevel, request);
            }

            @Override
            protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {
                return super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
            }

            @Override
            protected IOException logIOException(String configKey, Level logLevel, IOException ioe, long elapsedTime) {
             if (LOGGER.isInfoEnabled()) {
                 return super.logIOException(configKey, logLevel, ioe, elapsedTime);
             }
             return ioe;
            }
        };
    }

    private Client disableSslValidation() {
        return new Client.Default(null, null);
    }

    private X509TrustManager x509TrustManager() {
        return new HandshakeCertificates.Builder().build().trustManager();
    }

    private SSLSocketFactory getSSLSocketFactory() {
        char[] allPassword = keyStorePassword.toCharArray();
        SSLContext sslContext = null;
        try {
            TrustStrategy trustStrategy = new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            };
            sslContext = SSLContextBuilder
                    .create()
                    .setKeyStoreType("JKS")
                    .loadKeyMaterial(ResourceUtils.getFile(keyStore), allPassword, allPassword)
                    .loadTrustMaterial(trustStrategy)
                    .build();
        } catch (Exception ignored) { }
        assert sslContext != null;
        return sslContext.getSocketFactory();
    }*/
}
