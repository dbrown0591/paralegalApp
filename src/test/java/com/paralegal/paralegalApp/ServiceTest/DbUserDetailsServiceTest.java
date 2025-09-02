package com.paralegal.paralegalApp.ServiceTest;

import com.paralegal.paralegalApp.Model.User;
import com.paralegal.paralegalApp.Repository.UserRepository;
import com.paralegal.paralegalApp.Service.DbUserDetailsService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import com.paralegal.paralegalApp.Model.User;
import com.paralegal.paralegalApp.Repository.UserRepository;
import com.paralegal.paralegalApp.Service.DbUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DbUserDetailsServiceTest {
    @Mock
    UserRepository userRepository;
    @InjectMocks
    DbUserDetailsService dbUserDetailsService;

    @Test
    void loadUserByUserName_found_buildsSpringUser(){
        var entity = User.builder().id(1L).userName("Devon").email("devon@example.com").password("ENCODED").build();

        when(userRepository.findByEmail("devon@example.com")).thenReturn(Optional.of(entity));

        UserDetails details = dbUserDetailsService.loadUserByUsername("devon@example.com");

        assertThat(details.getUsername()).isEqualTo("devon@example.com");
        assertThat(details.getPassword()).isEqualTo("ENCODED");

        Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER"); // because .roles("USER")

        verify(userRepository).findByEmail("devon@example.com");
        verifyNoMoreInteractions(userRepository);

    }

    @Test
    void loadUserByUserName_notFound_throws(){
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->dbUserDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@example.com");

        verify(userRepository).findByEmail("missing@example.com");
    }
}
