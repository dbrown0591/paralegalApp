package com.paralegal.paralegalApp.Service;

import com.paralegal.paralegalApp.Exceptions.UserNotFoundException;
import com.paralegal.paralegalApp.Model.User;
import com.paralegal.paralegalApp.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository; // TODO: Convert to constructor injection once finalized if not using multiple constuctors

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUserById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User createUser(User user){
        return userRepository.save(user);
    }

    public User updateUser(Long id, User updateUser){
            return userRepository.findById(id)
                    .map(existingUser -> {
                        updateUser.setID(id);
                        return userRepository.save(updateUser);
                    })
                    .orElseThrow(() -> new UserNotFoundException("User Not Found"));
    }

    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }
}
