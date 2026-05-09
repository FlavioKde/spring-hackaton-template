package com.interhack.spring_hackaton_template.dto;

import com.interhack.spring_hackaton_template.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String name;
    private UserRole role;
}
