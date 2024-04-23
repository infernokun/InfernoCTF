package com.infernokun.services;

import com.infernokun.models.entities.User;
import com.infernokun.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("user is not valid"));
    }

    public User findUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("user is not valid"));
    }

    public void registerUser(User user) {
        String encodedPassword  = this.passwordEncoder.encode(user.getPassword());

        user.setPassword(encodedPassword);
        this.userRepository.save(user);
    }

    public boolean authenticatedUser(String username, String password) {
        String encodedPassword = "";

        return this.passwordEncoder.matches(password, encodedPassword);
    }
    public Optional<List<User>> findAllUsers() {
        return Optional.of(this.userRepository.findAll());
    }

    public boolean existsByUsernameOrEmail(User user) {
        return this.userRepository.existsByUsername(user.getUsername()) || this.userRepository.existsByEmail(user.getEmail());
    }
}
