package com.paralegal.paralegalApp.ServiceTest;

import com.paralegal.paralegalApp.Exceptions.UserNotFoundException;
import com.paralegal.paralegalApp.Model.User;
import com.paralegal.paralegalApp.Repository.UserRepository;
import com.paralegal.paralegalApp.Service.UserService;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    ArgumentCaptor<User> userCaptor;

    private User existing;

    @BeforeEach
    void setUp() {
        existing = User.builder()
                .id(1L)
                .userName("devon")
                .email("devon@example.com")
                .password("oldpass")
                .build();
    }

    @Test
    void getAllUsers_returnList() {
        when(userRepository.findAll()).thenReturn(List.of(existing));

        var list = userService.getAllUsers();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(1L);
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_returnsOptional() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        var result = userService.getUserById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getUserName()).isEqualTo("devon");
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_notFound_returnsEmpty() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        var result = userService.getUserById(99L);

        assertThat(result).isEmpty();
        verify(userRepository).findById(99L);
    }

    @Test
    void createUser_hashesPassword_andSaves_withGeneratedId() {
        // arrange
        when(passwordEncoder.encode("secret1")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setId(10L);
            return u;
        });

        // act
        var saved = userService.createUser(
                User.builder()
                        .userName("new")
                        .email("n@mail.com")
                        .password("secret1")
                        .build()
        );

        // assert
        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getPassword()).isEqualTo("HASHED");

        verify(passwordEncoder).encode("secret1");
        verify(userRepository).save(any(User.class));
    }


    @Test
    void updateUser_found_setsIdAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var incoming = User.builder()
                .userName("changed")
                .email("devon@example.com")
                .password("oldpass")
                .build();
        var result = userService.updateUser(1L, incoming);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserName()).isEqualTo("changed");
        verify(userRepository).findById(1L);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void updateUser_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, new User()))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any());

    }
    @Test
    void partiallyUpdateUser_updatesUserName_butNotEmailOrPassword(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> updates = Map.of(
                "userName", "robin",
                "email", "hacker@time.com",
                "password", "newpassAttempt"
        );

        var patched = userService.partiallyUpdateUser(1L,updates);

        assertThat(patched.getUserName()).isEqualTo("robin");
        assertThat(patched.getEmail()).isEqualTo("devon@example.com");
        assertThat(patched.getPassword()).isEqualTo("oldpass");

        verify(userRepository).save(userCaptor.capture());
        var saved = userCaptor.getValue();
        assertThat(saved.getUserName()).isEqualTo("robin");
        assertThat(saved.getEmail()).isEqualTo("devon@example.com");
        assertThat(saved.getPassword()).isEqualTo("oldpass");
    }
    @Test
    void partiallyUpdateUser_notFound_throws(){
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(()-> userService.partiallyUpdateUser(99L,Map.of("userName", "x")))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any());
    }
    @Test
    void partiallyUpdatedUser_updatesUserName_butNotEmailOrPassword(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> updates = Map.of(

                "userName", "robin",
                "email","hacker@evil.com",
                "password", "newpassAttempt"
        );

        var patched = userService.partiallyUpdateUser(1L,updates);

        assertThat(patched.getUserName()).isEqualTo("robin");
        assertThat(patched.getEmail()).isEqualTo("devon@example.com");
        assertThat(patched.getPassword()).isEqualTo("oldpass");

        verify(userRepository).save(userCaptor.capture());
        var saved = userCaptor.getValue();
        assertThat(saved.getUserName()).isEqualTo("robin");
        assertThat(saved.getEmail()).isEqualTo("devon@example.com");
        assertThat(saved.getPassword()).isEqualTo("oldpass");
    }
    @Test
    void partiallyUpdatedUser_notFound_throws(){
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(()-> userService.partiallyUpdateUser(99L,Map.of("userName","x")))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any());
    }
    @Test
    void updateEmail_found_setsEmailAndSaves(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        userService.updateEmail(1L,"new@email.com");

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("new@email.com");
    }
    @Test
    void updateEmail_notFound_throws(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(()-> userService.updateEmail(1L,"old@mail.com"))
                .isInstanceOf(UserNotFoundException.class);
         verify(userRepository, never()).save(any());
    }
    @Test
    void updatePassword_found_setsPasswordAndSaves(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updatePassword(1L,"password");

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("password");
    }
    @Test
    void updatePassword_notFound_throws(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(()-> userService.updatePassword(1L,"password"))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepository, never()).save(any());
    }
    @Test
    void deleteUser_delegatesToRepo(){

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}