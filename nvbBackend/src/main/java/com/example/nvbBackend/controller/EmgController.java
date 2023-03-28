package com.example.nvbBackend.controller;

import com.example.nvbBackend.model.User;
import com.example.nvbBackend.repository.EmgRepository;
import com.example.nvbBackend.service.EmgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin
@RestController
@RequestMapping("api")
public class EmgController {
    @Autowired
    private EmgService emgService;

    @PostMapping("/save")
    public ResponseEntity<User> saveUser(@RequestBody User user){
        User savedUser=emgService.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }
}