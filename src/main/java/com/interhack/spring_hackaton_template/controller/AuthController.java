package com.interhack.spring_hackaton_template.controller;

import com.interhack.spring_hackaton_template.dto.LoginRequest;
import com.interhack.spring_hackaton_template.dto.LoginResponse;
import com.interhack.spring_hackaton_template.dto.RegisterRequest;
import com.interhack.spring_hackaton_template.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
}
