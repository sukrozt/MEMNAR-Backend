package org.memnar.backend.security.service;

import org.memnar.backend.security.dto.AuthenticationRequest;
import org.memnar.backend.security.dto.AuthenticationResponse;
import org.memnar.backend.security.dto.RegisterRequest;
import org.memnar.backend.security.model.User;
import org.memnar.backend.security.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        repository.save(user);
        
        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        User user = repository.findByUsername(request.getUsername()).orElseThrow();
        
        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }
}