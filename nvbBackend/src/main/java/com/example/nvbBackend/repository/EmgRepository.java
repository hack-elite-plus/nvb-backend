package com.example.nvbBackend.repository;

import com.example.nvbBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmgRepository extends JpaRepository <User,Long>{
}
