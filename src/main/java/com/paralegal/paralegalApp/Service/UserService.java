package com.paralegal.paralegalApp.Service;

import com.paralegal.paralegalApp.Exceptions.UserNotFoundException;
import com.paralegal.paralegalApp.Model.User;
import com.paralegal.paralegalApp.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository; // TODO: Convert to constructor injection once finalized if not using multiple constuctors

    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id){
        return userRepository.findById(id);
    }

    public User createUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(Long id, User updateUser){
            return userRepository.findById(id)
                    .map(existingUser -> {
                        updateUser.setId(id);
                        return userRepository.save(updateUser);
                    })
                    .orElseThrow(() -> new UserNotFoundException("User Not Found"));
    }
    @SuppressWarnings("ConstantConditions")
    public User partiallyUpdateUser(Long id, Map<String,Object> updates){
        User existingUser = userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException("User not Found: " + id));
        updates.forEach((key, value)-> {
            if(key.equals("email") || key.equals("password")){
                return; //Do not update these here
            }
            Field field = ReflectionUtils.findField(User.class, key);
            if(field != null){
                field.setAccessible(true);
                ReflectionUtils.setField(field, existingUser, value);
            }
        });
        return userRepository.save(existingUser);
    }

    public void updateEmail(Long id, String newEmail){
        User user = userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException("User Not Found with Id: " + id));
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    public void updatePassword(Long id, String newPassword){
        User user = userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException("User Not Found with Id: " + id));
        // TODO: Hash the Password before saving (Spring Security BCrypt or Similar)
        // TODO: Validation should also be added- like email format, password strength, etc
        user.setPassword(newPassword);
        userRepository.save(user);
    }
    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }
}
