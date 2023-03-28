package com.example.nvbBackend.service;

import com.example.nvbBackend.model.User;
import com.example.nvbBackend.repository.EmgRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmgServiceImpl implements EmgService{
    @Autowired
    private EmgRepository emgRepository;

    @Autowired
    public EmgServiceImpl(EmgRepository emgRepository){
        this.emgRepository=emgRepository;
    }

    @Override
    public User save(User user) {
        return emgRepository.save(user);
    }
}
