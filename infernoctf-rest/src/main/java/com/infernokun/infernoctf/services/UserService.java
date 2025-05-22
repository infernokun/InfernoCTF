package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.exceptions.ResourceNotFoundException;
import com.infernokun.infernoctf.models.entities.User;
import com.infernokun.infernoctf.models.enums.Role;
import com.infernokun.infernoctf.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User findUserById(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    public boolean registerUser(User user) {
        String encodedPassword  = this.passwordEncoder.encode(user.getPassword());

        user.setPassword(encodedPassword);
        this.userRepository.save(user);

        return true;
    }

    public boolean createManyUsers(List<User> users) {
        users.forEach(this::registerUser);
        return true;
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public User updateUser(String id, User user) {
        User existingUser = findUserById(id);

        existingUser.setUsername(user.getUsername());
        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);

    }

    public boolean authenticatedUser(String username, String password) {
        String encodedPassword = "";

        return passwordEncoder.matches(password, encodedPassword);
    }
    public List<User> findAllUsers() {
        return this.userRepository.findAll();
    }

    public User deleteUser(String id) {
        User deletedUser = findUserById(id);
        userRepository.deleteById(deletedUser.getId());
        return deletedUser;
    }

    public boolean existsByUsernameOrEmail(User user) {
        return userRepository.existsByUsername(user.getUsername()) || userRepository.existsByEmail(user.getEmail());
    }
}
