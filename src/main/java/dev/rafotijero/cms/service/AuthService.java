package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.LoginResponse;
import dev.rafotijero.cms.domain.User;
import dev.rafotijero.cms.repository.UserRepository;
import dev.rafotijero.cms.security.IssuedToken;
import dev.rafotijero.cms.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));

        IssuedToken issuedToken = jwtService.generate(user.getUsername(), user.getRole().name());
        return new LoginResponse(issuedToken.token(), issuedToken.expiresAt());
    }
}
