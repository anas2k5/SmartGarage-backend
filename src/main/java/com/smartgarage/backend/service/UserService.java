package com.smartgarage.backend.service;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public User registerUser(User u) {
        if (repo.findByEmail(u.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        u.setPassword(encoder.encode(u.getPassword()));
        u.setActive(true); // default new users active
        return repo.save(u);
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }
}
