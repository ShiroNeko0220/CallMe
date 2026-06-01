package fr.miage.toulouse.callme.libcommun;

import org.springframework.http.HttpStatus;
import java.util.Arrays;

public final class RoleCheck {
    private RoleCheck() {}

    public static void require(String roleHeader, Role... allowed) {
        if (roleHeader == null || roleHeader.isBlank()) throw new ApiException(HttpStatus.FORBIDDEN, "Rôle manquant: header X-Role requis");
        Role current;
        try {
            current = Role.valueOf(roleHeader);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Rôle inconnu");
        }
        if (Arrays.stream(allowed).noneMatch(r -> r == current)) throw new ApiException(HttpStatus.FORBIDDEN, "Accès refusé");
    }
}
