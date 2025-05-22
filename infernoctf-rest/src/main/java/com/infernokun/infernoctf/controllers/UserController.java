package com.infernokun.infernoctf.controllers;

import com.infernokun.infernoctf.models.ApiResponse;
import com.infernokun.infernoctf.models.entities.User;
import com.infernokun.infernoctf.models.enums.Role;
import com.infernokun.infernoctf.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(ApiResponse.<List<User>>builder()
                .code(HttpStatus.OK.value())
                .message("Users retrieved successfully.")
                .data(users)
                .build());
    }

    @GetMapping("/by")
    public ResponseEntity<ApiResponse<User>> getUserBy(@RequestParam Map<String, String> params) {
        User user = null;
        String message = "User retrieved successfully.";

        if (params.containsKey("username")) {
            String username = params.get("username");
            user = this.userService.findUserByUsername(username);
            message = String.format("User with username '%s' retrieved successfully.", username);

        } else if (params.containsKey("id")) {
            String userId = params.get("id");
            user = this.userService.findUserById(userId);
            message = String.format("User with ID '%s' retrieved successfully.", userId);

        } else {
            throw new IllegalArgumentException("At least one search parameter is required: username, or id");
        }

        return ResponseEntity.ok(
                ApiResponse.<User>builder()
                        .code(HttpStatus.OK.value())
                        .message(message)
                        .data(user)
                        .build()
        );
    }

    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<User>builder()
                .code(HttpStatus.OK.value())
                .message("Retrieved user!")
                .data(userService.findUserById(id))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createUser(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<Boolean>builder()
                .code(HttpStatus.CREATED.value())
                .message("User created successfully.")
                .data(userService.registerUser(user))
                .build());
    }

    @PostMapping("/many")
    public ResponseEntity<ApiResponse<Boolean>> createManyUsers(@RequestBody List<User> users) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<Boolean>builder()
                .code(HttpStatus.CREATED.value())
                .message("Users created successfully.")
                .data(userService.createManyUsers(users))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> deleteUser(@PathVariable String id) {
        User deletedUser = userService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.<User>builder()
                .code(HttpStatus.OK.value())
                .message("User deleted successfully.")
                .data(deletedUser)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable String id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);

        return ResponseEntity.ok(ApiResponse.<User>builder()
                .code(HttpStatus.OK.value())
                .message("User updated successfully.")
                .data(updatedUser)
                .build());

    }
}
