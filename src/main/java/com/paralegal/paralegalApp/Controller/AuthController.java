package com.paralegal.paralegalApp.Controller;

import com.paralegal.paralegalApp.DTO.AuthRequest;
import com.paralegal.paralegalApp.DTO.AuthResponse;
import com.paralegal.paralegalApp.DTO.RegisterRequest;
import com.paralegal.paralegalApp.Model.User;
import com.paralegal.paralegalApp.Repository.UserRepository;
import com.paralegal.paralegalApp.Security.JwtService;
import com.paralegal.paralegalApp.Enum.Role;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
        if(users.existsByEmail(request.email())){
            return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));
        }

        User saved = users.save(User.builder()
                .userName(request.userName())
                .email(request.email())
                .password(encoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build());

        String token = jwt.generateToken(saved.getEmail(),Map.of("role", saved.getRole().name(), "uid", saved.getId()));
        return ResponseEntity.ok(new AuthResponse(token));
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req){
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        String token = jwt.generateToken(req.email(), Map.of("role", auth.getAuthorities().iterator().next().getAuthority()));

        return ResponseEntity.ok(new AuthResponse(token));
    }


}
