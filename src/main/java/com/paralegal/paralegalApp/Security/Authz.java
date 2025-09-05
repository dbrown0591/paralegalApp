package com.paralegal.paralegalApp.Security;

import com.paralegal.paralegalApp.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("authz")
@RequiredArgsConstructor
public class Authz {

    private final UserRepository userRepository;

    /**
     * Returns true if the authenticated user’s id == targetUserId.
     * Used in: @PreAuthorize("hasRole('ADMIN') or @authz.isSelf(authentication, #id)")
     */
    public boolean isSelf(Authentication authentication, Long targetUserId) {
        if (authentication == null || targetUserId == null) return false;
        var email = authentication.getName(); // we use email as username
        return userRepository.findByEmail(email)
                .map(u -> u.getId().equals(targetUserId))
                .orElse(false);
    }
}

