package io.chirper.common;

import java.security.Principal;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public class PrincipalUtil {
    public static UUID getUserId(Principal principal) {
        return UUID.fromString(principal.getName());
    }
}
