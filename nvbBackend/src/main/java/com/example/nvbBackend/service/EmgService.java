package com.example.nvbBackend.service;

import com.example.nvbBackend.model.User;
import com.example.nvbBackend.repository.EmgRepository;

import java.util.List;

public interface EmgService {
    User save(User user);
}
