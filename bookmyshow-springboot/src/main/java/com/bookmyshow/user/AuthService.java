package com.bookmyshow.user;

import com.bookmyshow.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest r) {
        if (users.findByEmail(r.email()).isPresent())
            throw new IllegalStateException("Email already registered");
        User u = users.save(new User(r.name(), r.email().toLowerCase(), encoder.encode(r.password())));
        return new AuthDtos.AuthResponse(jwt.generate(u.getEmail()), u.getId(), u.getEmail());
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest r) {
        User u = users.findByEmail(r.email().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!encoder.matches(r.password(), u.getPasswordHash()))
            throw new IllegalArgumentException("Invalid credentials");
        return new AuthDtos.AuthResponse(jwt.generate(u.getEmail()), u.getId(), u.getEmail());
    }
}
