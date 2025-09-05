package com.paralegal.paralegalApp.Service;

import com.paralegal.paralegalApp.Enum.Role;
import com.paralegal.paralegalApp.Model.User;
import com.paralegal.paralegalApp.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user: " + email));

        Set<Role> roles = Optional.ofNullable(u.getRoles())
                .filter(r -> !r.isEmpty())
                .orElse(Set.of(Role.ROLE_USER)); // <- default

        var authorities = roles.stream()
                .map(Enum::name)                    // "ROLE_USER"
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPassword())
                .authorities(authorities)
                .accountExpired(false).accountLocked(false)
                .credentialsExpired(false).disabled(false)
                .build();
    }
}
