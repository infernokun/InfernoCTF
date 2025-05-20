package com.infernokun.config.websocket;

import feign.Client;
import lombok.Getter;
import lombok.Setter;
import okhttp3.tls.HandshakeCertificates;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.tomcat.websocket.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
@Getter
@Setter
public class ClientConfig {
    @Value("${ssl.keystore.uri}")
    private String keyStore;
    @Value("${ssl.keystore.password}")
    private String keyStorePassword;

    public StandardWebSocketClient establishClient() {
        SSLContext sslContext = getSSLSocketFactory();

        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        webSocketClient.getUserProperties().put(Constants.SSL_CONTEXT_PROPERTY, sslContext);
        webSocketClient.getUserProperties().put(Constants.SSL_TRUSTSTORE_PROPERTY, keyStore);
        return webSocketClient;

    }

    private Client disableSslValidation() {
        return new Client.Default(null, null);
    }

    private X509TrustManager x509TrustManager() {
        return new HandshakeCertificates.Builder().build().trustManager();
    }

    private SSLContext getSSLSocketFactory() {
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
        return sslContext;
    }
}
