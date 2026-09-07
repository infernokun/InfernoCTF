package com.infernokun.infernoctf.utils;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Set;

/**
 * The RSA key pair that signs and verifies access tokens, read from {@code app.jwt.key-dir}
 * and generated there on first start.
 *
 * <p>The directory must be persistent: a key that changes on each boot invalidates every token
 * in circulation, and two instances with different keys cannot verify each other's. If it is
 * not writable the key stays in memory and startup warns, rather than failing, so that a plain
 * {@code ./gradlew bootRun} still works.
 */
@Component
@Getter
public class RSAKeyProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(RSAKeyProperties.class);
    private static final String PRIVATE_KEY_FILE = "jwt-private.pem";
    private static final String PUBLIC_KEY_FILE = "jwt-public.pem";

    private final java.security.interfaces.RSAPublicKey publicKey;
    private final java.security.interfaces.RSAPrivateKey privateKey;

    public RSAKeyProperties(@Value("${app.jwt.key-dir:/data/certs/jwt}") String keyDir) {
        Path dir = Path.of(keyDir);
        Path privatePath = dir.resolve(PRIVATE_KEY_FILE);
        Path publicPath = dir.resolve(PUBLIC_KEY_FILE);

        KeyPair pair = tryLoad(privatePath, publicPath);
        if (pair == null) {
            pair = KeyGeneratorUtility.generateRsaKey();
            persist(dir, privatePath, publicPath, pair);
        }

        this.publicKey = (java.security.interfaces.RSAPublicKey) pair.getPublic();
        this.privateKey = (java.security.interfaces.RSAPrivateKey) pair.getPrivate();
    }

    private KeyPair tryLoad(Path privatePath, Path publicPath) {
        if (!Files.isReadable(privatePath) || !Files.isReadable(publicPath)) {
            return null;
        }
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            var priv = factory.generatePrivate(new PKCS8EncodedKeySpec(readPem(privatePath)));
            var pub = factory.generatePublic(new X509EncodedKeySpec(readPem(publicPath)));
            LOGGER.info("Loaded the JWT signing key from {}", privatePath);
            return new KeyPair(pub, priv);
        } catch (IOException | GeneralSecurityException ex) {
            // Fail rather than silently regenerate, which would log everyone out on each boot.
            throw new IllegalStateException(
                    "JWT signing key at " + privatePath + " could not be read. Fix or remove it.", ex);
        }
    }

    private void persist(Path dir, Path privatePath, Path publicPath, KeyPair pair) {
        try {
            Files.createDirectories(dir);
            writePem(privatePath, "PRIVATE KEY", pair.getPrivate().getEncoded());
            writePem(publicPath, "PUBLIC KEY", pair.getPublic().getEncoded());
            restrictPermissions(privatePath);
            LOGGER.info("Generated a new JWT signing key and stored it at {}", privatePath);
        } catch (IOException ex) {
            LOGGER.warn("Could not persist the JWT signing key to {} ({}). Falling back to an "
                            + "in-memory key: every restart will invalidate all issued tokens, and "
                            + "running more than one instance will not work. Set app.jwt.key-dir "
                            + "(APP_JWT_KEY_DIR) to a writable, persistent path.",
                    dir, ex.getMessage());
        }
    }

    private static byte[] readPem(Path path) throws IOException {
        String pem = Files.readString(path, StandardCharsets.UTF_8);
        String body = pem.replaceAll("-----(BEGIN|END)[^-]*-----", "").replaceAll("\\s", "");
        return Base64.getDecoder().decode(body);
    }

    private static void writePem(Path path, String label, byte[] der) throws IOException {
        String base64 = Base64.getMimeEncoder(64, System.lineSeparator().getBytes(StandardCharsets.UTF_8))
                .encodeToString(der);
        Files.writeString(path, "-----BEGIN " + label + "-----" + System.lineSeparator()
                + base64 + System.lineSeparator()
                + "-----END " + label + "-----" + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    private static void restrictPermissions(Path path) {
        try {
            Files.setPosixFilePermissions(path, Set.of(PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE));
        } catch (IOException | UnsupportedOperationException ex) {
            LOGGER.debug("Could not restrict permissions on {}: {}", path, ex.getMessage());
        }
    }
}
