package com.paralegal.paralegalApp.Controller;

import com.paralegal.paralegalApp.Exceptions.UserNotFoundException;
import com.paralegal.paralegalApp.Model.User;
import com.paralegal.paralegalApp.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//@Validated  -- Circle back
@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user){
        User savedUser = userService.createUser(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('ADMIN') or @authz.isSelf(authentication, #id)")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        return userService.getUserById(id).map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseThrow(()-> new UserNotFoundException("User Not Found: " + id));
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.isSelf(authentication, #id)")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user){
        User  updateUser = userService.updateUser(id,user);
        return new ResponseEntity<>(updateUser, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('ADMIN') or @authz.isSelf(authentication, #id)")
    @PatchMapping("/{id}")
    public ResponseEntity<User> partiallyUpdateUser(@PathVariable long id, Map<String, Object> updates){
        User updateUser = userService.partiallyUpdateUser(id,updates);
        return new ResponseEntity<>(updateUser, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('ADMIN') or @authz.isSelf(authentication, #id)")
    @PatchMapping("/{id}/update-email")
    public ResponseEntity<String> updateEmail(@PathVariable Long id, @RequestBody Map<String,String> body ){
        String newEmail = body.get("email");
        userService.updateEmail(id, newEmail);
        return ResponseEntity.ok("Email updated successfully. ");
    }
    @PreAuthorize("hasRole('ADMIN') or @authz.isSelf(authentication, #id)")
    @PatchMapping("/{id}/update-password")
    public ResponseEntity<String> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> body){
        String newPassword = body.get("password");
        userService.updatePassword(id,newPassword );
        return ResponseEntity.ok("Password updated successfully. ");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
