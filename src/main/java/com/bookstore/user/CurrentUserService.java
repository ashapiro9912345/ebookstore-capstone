package com.bookstore.user;

import com.bookstore.common.exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Resolves the currently authenticated {@link User} entity from the Spring Security context.
 *
 * The JWT filter stores the customer's email as the authentication principal name;
 * this service loads the full User entity for that email.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @return the authenticated customer's User entity
     * @throws ResourceNotFoundException if no matching user exists (should not happen for a valid token)
     */
    public User requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user in security context");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found: " + email));
    }
}
