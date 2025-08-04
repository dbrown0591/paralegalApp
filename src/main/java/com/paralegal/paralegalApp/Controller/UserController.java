package com.paralegal.paralegalApp.Controller;

import com.paralegal.paralegalApp.Exceptions.UserNotFoundException;
import com.paralegal.paralegalApp.Model.User;
import com.paralegal.paralegalApp.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
        User savedUser = userService.createUser(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        return userService.getUserById(id).map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseThrow(()-> new UserNotFoundException("User Not Found: " + id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user){
        User  updateUser = userService.updateUser(id,user);
        return new ResponseEntity<>(updateUser, HttpStatus.OK);
    }
    @PatchMapping("/{id}")
    public ResponseEntity<User> partiallyUpdateUser(@PathVariable long id, Map<String, Object> updates){
        User updateUser = userService.partiallyUpdateUser(id,updates);
        return new ResponseEntity<>(updateUser, HttpStatus.OK);
    }

    @PatchMapping("/{id}/update-email")
    public ResponseEntity<String> updateEmail(@PathVariable Long id, @RequestBody Map<String,String> body ){
        String newEmail = body.get("email");
        userService.updateEmail(id, newEmail);
        return ResponseEntity.ok("Email updated successfully. ");
    }

    @PatchMapping("/{id}/update-password")
    public ResponseEntity<String> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> body){
        String newPassword = body.get("password");
        userService.updatePassword(id,newPassword );
        return ResponseEntity.ok("Password updated successfully. ");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
