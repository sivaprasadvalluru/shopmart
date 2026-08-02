package com.shopmart.dto;

import com.shopmart.model.enums.Role;

public record AuthResponse(
        String token,
        String email,
        Role role
) {
}
