package com.example.user_service.controller;

import com.example.user_service.dto.UserInforDTO;
import com.example.user_service.model.User;
import com.example.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<User> getUser(){
        return ResponseEntity.ok(userService.findUserByJwtToken());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) throws Exception {
        return ResponseEntity.ok(userService.findUserByUserId(id));
    }

    @PutMapping("/information")
    public ResponseEntity<User> updateUserInformation(@RequestBody UserInforDTO userInforDTO){
        User user = userService.findUserByJwtToken();
        return ResponseEntity.ok(userService.updateUserInformation(userInforDTO, user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUser(){
        return ResponseEntity.ok(userService.getAllUser());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sellers")
    public ResponseEntity<List<User>> getAllSeller(){
        return ResponseEntity.ok(userService.getAllSeller());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/block")
    public ResponseEntity<User> blockUser(@RequestParam String userId){
        return ResponseEntity.ok(userService.blockUser(userId));
    }
}
