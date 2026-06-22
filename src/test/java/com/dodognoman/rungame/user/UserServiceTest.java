package com.dodognoman.rungame.user;

import com.dodognoman.rungame.authjwt.JwtService;
import com.dodognoman.rungame.user.dto.AuthResponse;
import com.dodognoman.rungame.user.dto.LoginRequest;
import com.dodognoman.rungame.user.dto.RegisterRequest;
import com.dodognoman.rungame.user.repo.User;
import com.dodognoman.rungame.user.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @InjectMocks UserService userService;

    private static final String CLIENT_IP = "127.0.0.1";

    @BeforeEach
    void setUp() {
        // stub 若未被呼叫會噴 noNecusuryException, 使用lenient() 關閉
        lenient().when(jwtService.generateAccessToken(any())).thenReturn("access.jwt.token");
        lenient().when(jwtService.accessTokenExpiresAt()).thenReturn(OffsetDateTime.now().plusHours(24));
    }

    // ── register ────────────────────────────────────────────────────────────

    @Test
    void register_success_returnsAuthResponse() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse resp = userService.register(new RegisterRequest("alice", "password123"), CLIENT_IP);

        assertThat(resp.username()).isEqualTo("alice");
        assertThat(resp.accessToken()).isEqualTo("access.jwt.token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateUsername_respNullValue() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        AuthResponse resp = userService.register(new RegisterRequest("alice", "password123"), CLIENT_IP);

        assertThat(resp).isNotNull();
        assertThat(resp.userId()).isNull();
        assertThat(resp.username()).isNull();
        assertThat(resp.role()).isNull();
        assertThat(resp.accessToken()).isNull();
        assertThat(resp.tokenExpiresAt()).isNull();

        verify(userRepository, never()).save(any());
    }

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    void login_success_returnsAuthResponse() {
        User user = buildActiveUser("alice", "password123");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse resp = userService.login(new LoginRequest("alice", "password123"), CLIENT_IP);

        assertThat(resp.username()).isEqualTo("alice");
        assertThat(resp.accessToken()).isEqualTo("access.jwt.token");
    }

    @Test
    void login_userNotFound_throwsUnauthorized() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.login(new LoginRequest("ghost", "password123"), CLIENT_IP))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(UNAUTHORIZED);
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        User user = buildActiveUser("alice", "password123");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                userService.login(new LoginRequest("alice", "wrongpassword"), CLIENT_IP))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(UNAUTHORIZED);
    }

    @Test
    void login_inactiveUser_throwsForbidden() {
        User user = buildActiveUser("alice", "password123");
        user.setActive(false);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                userService.login(new LoginRequest("alice", "password123"), CLIENT_IP))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(FORBIDDEN);
    }

    @Test
    void login_updatesLastLoginIpAndAt() {
        User user = buildActiveUser("alice", "password123");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.login(new LoginRequest("alice", "password123"), "192.168.1.1");

        assertThat(user.getLastLoginIp()).isEqualTo("192.168.1.1");
        assertThat(user.getLastLoginAt()).isNotNull();
    }

    private User buildActiveUser(String username, String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(rawPassword));
        user.setActive(true);
        return user;
    }
}
