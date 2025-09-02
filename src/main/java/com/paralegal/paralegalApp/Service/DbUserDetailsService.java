package com.paralegal.paralegalApp.Service;

import com.paralegal.paralegalApp.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Primary
@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

private final UserRepository userRepository;
@Override
public UserDetails loadUserByUsername(String username){

    var user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("No user: " + username));
    return User
            .withUsername(user.getEmail())
            .password(user.getPassword())
            .roles("USER")
            .build();
}

}
