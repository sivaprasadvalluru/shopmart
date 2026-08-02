package com.shopmart.testsupport;

import com.shopmart.model.entity.User;
import com.shopmart.security.JwtTokenProvider;
import com.shopmart.security.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Small helper shared by the {@code @SpringBootTest} controller tests to mint a valid JWT
 * for a persisted {@link User} without going through the full login HTTP flow every time.
 */
public final class AuthTestUtils {

    private AuthTestUtils() {
    }

    public static String tokenFor(JwtTokenProvider jwtTokenProvider, User user) {
        UserPrincipal principal = UserPrincipal.fromUser(user);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        return jwtTokenProvider.generateToken(authentication);
    }

    public static String bearer(JwtTokenProvider jwtTokenProvider, User user) {
        return "Bearer " + tokenFor(jwtTokenProvider, user);
    }
}
