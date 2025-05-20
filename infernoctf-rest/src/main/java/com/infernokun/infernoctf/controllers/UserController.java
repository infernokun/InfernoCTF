package com.infernokun.infernoctf.controllers;

import com.infernokun.infernoctf.models.entities.User;
import com.infernokun.infernoctf.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("user")
public class UserController {
    private final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /*@GetMapping("{username}")
    public ResponseEntity<User> lol(@PathVariable String roomId)*/

    @GetMapping
    public ResponseEntity<User> getUserByUsername(@RequestParam("username") String username) {
        return ResponseEntity.ok(this.userService.findUserByUsername(username));
    }

    @GetMapping("all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(this.userService.findAllUsers().orElse(Collections.emptyList()));
    }
}
