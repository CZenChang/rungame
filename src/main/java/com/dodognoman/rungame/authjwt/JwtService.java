package com.dodognoman.rungame.authjwt;

import com.dodognoman.rungame.user.repo.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;


@Service
public class JwtService {

    private final SecretKey key;
    private final int accessTokenHours;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-hours:24}") int accessTokenHours
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenHours = accessTokenHours;
    }

    public String generateAccessToken(User user) {
        return buildToken(user, accessTokenHours * 3600L, "access");
    }

    public OffsetDateTime accessTokenExpiresAt() {
        return OffsetDateTime.now(ZoneOffset.UTC).plusHours(accessTokenHours);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(User user, long ttlSeconds, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlSeconds * 1000);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claims(Map.of(
                        "username", user.getUsername(),
                        "role", user.getRole(),
                        "type", type
                ))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }
}
