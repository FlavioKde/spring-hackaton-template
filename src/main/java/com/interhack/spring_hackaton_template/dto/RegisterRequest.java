package com.interhack.spring_hackaton_template.dto;

import com.interhack.spring_hackaton_template.model.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String name;
    private String email;
    @NotNull
    private UserRole role;
}
