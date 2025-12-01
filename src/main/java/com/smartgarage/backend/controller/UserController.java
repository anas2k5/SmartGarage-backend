package com.smartgarage.backend.controller;

import com.smartgarage.backend.dto.UserDto;
import com.smartgarage.backend.model.User;
import com.smartgarage.backend.service.UserService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService us){ this.userService = us; }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserDto dto) {
        User u = User.builder().fullName(dto.fullName()).email(dto.email()).password(dto.password()).role(dto.role()).build();
        User saved = userService.registerUser(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
