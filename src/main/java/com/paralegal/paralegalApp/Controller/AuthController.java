package com.paralegal.paralegalApp.Controller;

import com.paralegal.paralegalApp.DTO.AuthRequest;
import com.paralegal.paralegalApp.DTO.AuthResponse;
import com.paralegal.paralegalApp.DTO.RegisterRequest;
import com.paralegal.paralegalApp.Enum.Role;
import com.paralegal.paralegalApp.Model.User;
import com.paralegal.paralegalApp.Repository.UserRepository;
import com.paralegal.paralegalApp.Security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(AuthenticationManager authManager, UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.authManager = authManager;
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request ){
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));
        }

        // Default new users to ROLE_USER (you can extend later)
        User saved = users.save(User.builder()
                .userName(request.userName())
                .email(request.email())
                .password(encoder.encode(request.password()))
                .roles(Set.of(Role.ROLE_USER))
                .build());

        // Build a roles list for the JWT claim
        List<String> roleNames = (saved.getRoles() == null || saved.getRoles().isEmpty())
                ? List.of("ROLE_USER")
                : saved.getRoles().stream().map(Enum::name).toList();

        String token = jwt.generateToken(
                saved.getEmail(),
                Map.of(
                        "roles", roleNames,     // <-- list of "ROLE_*"
                        "uid", saved.getId()
                )
        );

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req){
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.password())
        );

        // Include ALL authorities, not just the first
        List<String> roleNames = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // "ROLE_*"
                .toList();

        String token = jwt.generateToken(
                req.email(),
                Map.of("roles", roleNames) // <-- list of "ROLE_*"
        );

        return ResponseEntity.ok(new AuthResponse(token));
    }
}
