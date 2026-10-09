package com.example.shipments.Auth;

import com.example.shipments.Exception.ApiExceptions.UnauthorizedException;

/**
 * Identidad de la request, tomada de los headers que inyecta el BFF tras validar
 * el JWT de Entra ID (mismo contrato que ms-user). Este servicio no debe
 * exponerse directo a internet: solo el BFF puede llegar a el.
 */
public record AuthContext(String userId, String role) {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String ROLE_HEADER = "X-User-Role";

    public static final String ROLE_WORKSHOP_ADMIN = "WORKSHOP_ADMIN";

    public AuthContext {
        if (userId == null || userId.isBlank()) {
            throw new UnauthorizedException("Falta el encabezado de identidad: " + USER_ID_HEADER);
        }
    }
    /// QUickFIX: no se valida el rol, porque el BFF puede no enviarlo. Se valida en cada endpoint que lo requiera.U
    public static final AuthContext of(String userId) {
        return new AuthContext(userId, null);
    }

    public boolean isAdmin() {
        return ROLE_WORKSHOP_ADMIN.equals(role);
    }
}
