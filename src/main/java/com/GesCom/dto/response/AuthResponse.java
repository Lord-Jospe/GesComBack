package com.GesCom.dto.response;

import com.GesCom.model.Rol;
import lombok.Builder;

@Builder
public record AuthResponse(
        String token
){
}

