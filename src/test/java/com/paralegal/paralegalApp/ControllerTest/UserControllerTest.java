package com.paralegal.paralegalApp.ControllerTest;

import com.paralegal.paralegalApp.Controller.UserController;
import com.paralegal.paralegalApp.Exceptions.UserNotFoundException;
import com.paralegal.paralegalApp.Model.User;
import com.paralegal.paralegalApp.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

        @Mock
        private UserService userService;
        private MockMvc mockMvc;
        @RestControllerAdvice
        static class ApiExceptionHandler{
            @ExceptionHandler(UserNotFoundException.class)
            ResponseEntity<String> handle(UserNotFoundException ex){
                return ResponseEntity.status(404).body(ex.getMessage());
            }
        }
        @BeforeEach
        void setup(){
            var controller = new UserController(userService);

            mockMvc = MockMvcBuilders
                    .standaloneSetup(controller)
                    .setControllerAdvice(new ApiExceptionHandler())
                    .build();
        }
        @Test
        void createUser_returns201_andBody() throws Exception{
            var created = User.builder().id(10L).userName("devon").email("devon@example.com").password("hashed").build();
            when(userService.createUser(any(User.class))).thenReturn(created);

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"userName\":\"devon\",\"email\":\"devon@example.com\",\"password\":\"secret1\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10L))
                    .andExpect(jsonPath("$.userName").value("devon"))
                    .andExpect(jsonPath("$.email").value("devon@example.com"));

            var captor = ArgumentCaptor.forClass(User.class);
            verify(userService).createUser(captor.capture());
            var sent = captor.getValue();

            org.assertj.core.api.Assertions.assertThat(sent.getUserName()).isEqualTo("devon");
            org.assertj.core.api.Assertions.assertThat(sent.getEmail()).isEqualTo("devon@example.com");
            org.assertj.core.api.Assertions.assertThat(sent.getPassword()).isEqualTo("secret1");
        }
        @Test
        void getAllUsers_returns200_andList() throws Exception{
            var u1 = User.builder().id(1L).userName("a").email("a@mail.com").build();
            var u2 = User.builder().id(2L).userName("b").email("b@mail.com").build();

            when(userService.getAllUsers()).thenReturn(List.of(u1,u2));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$",hasSize(2)))
                    .andExpect(jsonPath("$.[0].id").value(1L))
                    .andExpect(jsonPath("$[0].userName").value("a"));
        }
        @Test
        void getUsersById_found_returns200() throws Exception{
            var user = User.builder().id(5L).userName("x").email("x@mail.com").build();
            when(userService.getUserById(5L)).thenReturn(Optional.of(user));

            mockMvc.perform(get("/api/users/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(5L))
                    .andExpect(jsonPath("$.userName").value("x"));
        }
        @Test
        void getUserById_notFound_returns404() throws Exception{
            when(userService.getUserById(404L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/404"))
                    .andExpect(status().isNotFound());
        }
        @Test
        void updateUser_returns200_andBody() throws Exception{
            var updated = User.builder()
                    .id(1L).userName("changed").email("devon@example.com").password("secret1")
                    .build();
            when(userService.updateUser(Mockito.eq(1L),any(User.class))).thenReturn(updated);

            mockMvc.perform(put("/api/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"userName\":\"changed\",\"email\":\"devon@example.com\",\"password\":\"secret1\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userName").value("changed"));
        }
        @Test
        void updateUser_notFound_returns404() throws Exception{
            when(userService.updateUser(Mockito.eq(404L), any(User.class)))
                    .thenThrow(new UserNotFoundException("User Not Found"));

            mockMvc.perform(put("/api/users/404")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"userName\":\"validName\",\"email\":\"a@b.com\",\"password\":\"secret1\"}"))
                    .andExpect(status().isNotFound());
        }
    @Test
    void partiallyUpdateUser_returns200_andBody() throws Exception{
            var patched = User.builder().id(3L).userName("patched").email("e@mail.com").password("p").build();
            when(userService.partiallyUpdateUser(Mockito.eq(3L), any())).thenReturn(patched);

            mockMvc.perform(patch("/api/users/3")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"userName\":\"patched\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3L))
                    .andExpect(jsonPath("$.userName").value("patched"));
    }
    @Test
    void partiallyUpdatedUser_notFound_returns404() throws Exception{
            when(userService.partiallyUpdateUser(Mockito.eq(77L), any(Map.class)))
                    .thenThrow(new UserNotFoundException("User Not Found"));

            mockMvc.perform(patch("/api/users/77")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"userName\":\"x\"}"))
                    .andExpect(status().isNotFound());
    }
    @Test
    void updateEmail_returns200_andMessage() throws Exception{
            doNothing().when(userService).updateEmail(5L, "new@mail.com");

            mockMvc.perform(patch("/api/users/5/update-email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"new@mail.com\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Email updated successfully")));
    }
    @Test
    void updateEmail_notFound_returns404() throws Exception{
            doThrow(new UserNotFoundException("User Not Found")).when(userService).updateEmail(9L,"x@mail.com");
            mockMvc.perform(patch("/api/users/9/update-email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"x@mail.com\"}"))
                    .andExpect(status().isNotFound());
        }
        @Test
        void updatePassword_return200_andMessage() throws Exception{
            doNothing().when(userService).updatePassword(5L, "newpass");

            mockMvc.perform(patch("/api/users/5/update-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"password\":\"newpass\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Password updated successfully")));
        }
        @Test
        void updatePassword_notFound_return404() throws Exception{
            doThrow(new UserNotFoundException("User Not Found")).when(userService).updatePassword(6L, "p");

            mockMvc.perform(patch("/api/users/6/update-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"password\":\"p\"}"))
                    .andExpect(status().isNotFound());
        }
        @Test
        void deleteUser_returns204() throws Exception{
            doNothing().when(userService).deleteUser(12L);

            mockMvc.perform(delete("/api/users/12"))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUser(12L);
        }
        @Test
        void createUser_invalidJson_returns400() throws Exception{
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{not-json"))
                    .andExpect(status().isBadRequest());
        }



}
