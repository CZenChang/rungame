package com.dodognoman.rungame.user;

import com.dodognoman.rungame.user.dto.AuthResponse;
import com.dodognoman.rungame.user.dto.LoginRequest;
import com.dodognoman.rungame.user.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req, HttpServletRequest httpRequest) {
        return userService.register(req, httpRequest.getRemoteAddr());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest httpRequest) {
        return userService.login(req, httpRequest.getRemoteAddr());
    }
}
