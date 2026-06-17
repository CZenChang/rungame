package com.dodognoman.rungame.user;

import com.dodognoman.rungame.common.JwtService;
import com.dodognoman.rungame.user.dto.AuthResponse;
import com.dodognoman.rungame.user.dto.LoginRequest;
import com.dodognoman.rungame.user.dto.RegisterRequest;
import com.dodognoman.rungame.user.repo.User;
import com.dodognoman.rungame.user.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional()
    public AuthResponse register(RegisterRequest req, String clientIp) {
        if (userRepository.existsByUsername(req.username())) {
            return new AuthResponse(null, null,null, null,null);
        }

        User user = new User();
        user.setUsername(req.username());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRegisterIp(clientIp);
        issueTokens(user);

        userRepository.save(user);
        return toAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req, String clientIp) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        user.setLastLoginAt(OffsetDateTime.now());
        user.setLastLoginIp(clientIp);
        issueTokens(user);

        userRepository.save(user);
        return toAuthResponse(user);
    }

    private void issueTokens(User user) {
        user.setAccessToken(jwtService.generateAccessToken(user));
        user.setTokenExpiresAt(jwtService.accessTokenExpiresAt());
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getAccessToken(),
                user.getTokenExpiresAt()
        );
    }
}
