package com.yuelutraffic.auth;

import com.yuelutraffic.common.ApiException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private static final long TOKEN_TTL_SECONDS = 7L * 24L * 60L * 60L;

    private final String tokenSecret;

    public TokenService(@Value("${app.security.token-secret}") String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public String issueToken(UUID userId) {
        long expiresAt = Instant.now().plusSeconds(TOKEN_TTL_SECONDS).getEpochSecond();
        String payload = userId + ":" + expiresAt;
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + sign(encodedPayload);
    }

    public UUID parseBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        String[] parts = token.split("\\.", 2);
        if (parts.length != 2 || !sign(parts[0]).equals(parts[1])) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid bearer token");
        }
        String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String[] fields = payload.split(":", 2);
        if (fields.length != 2) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid bearer token payload");
        }
        long expiresAt = Long.parseLong(fields[1]);
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Expired bearer token");
        }
        return UUID.fromString(fields[0]);
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign token", ex);
        }
    }
}
