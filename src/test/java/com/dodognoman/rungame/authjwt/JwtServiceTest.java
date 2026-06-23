package com.dodognoman.rungame.authjwt;

import com.dodognoman.rungame.user.repo.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static java.time.temporal.ChronoUnit.MINUTES;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-chars!!";
    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 24);

        user = new User();
        user.setUsername("alice");
        user.setRole("USER");
    }

    @Test
    void generateAccessToken_containsExpectedClaims() {
        String token = jwtService.generateAccessToken(user);

        Claims claims = jwtService.parseClaims(token);
        assertThat(claims.get("username")).isEqualTo("alice");
        assertThat(claims.get("role")).isEqualTo("USER");
        assertThat(claims.get("type")).isEqualTo("access");
    }


    @Test
    void generateAccessToken_expiresInConfiguredHours() {
        String token = jwtService.generateAccessToken(user);

        Claims claims = jwtService.parseClaims(token);
        long ttlMillis = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        long ttlHours = ttlMillis / 3600_000;
        assertThat(ttlHours).isEqualTo(24);
    }

    @Test
    void accessTokenExpiresAt_isApproximately24HoursFromNow() {
        OffsetDateTime expiresAt = jwtService.accessTokenExpiresAt();

        OffsetDateTime expected = OffsetDateTime.now().plusHours(24);
        assertThat(expiresAt).isCloseTo(expected, within(1, MINUTES));
    }


}
