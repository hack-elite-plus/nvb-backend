package com.example.mygoal.service;

import com.example.mygoal.model.Goal;
import com.example.mygoal.repositary.GoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoalServiceImpl implements GoalService{
    @Autowired
    private GoalRepository goalRepository;



    @Override
    public List<Goal> getAllGoal() {
        return goalRepository.findAll();
    }

    @Override
    public String getGoalValue(String sportType) {
        return goalRepository.findBySportType(sportType);
    }

    @Override
    public void saveUnique(String sportType, String goalType, String timeframe, String value) {
        goalRepository.saveUnique(sportType, goalType, timeframe, value);
    }

    public void deleteBySportType(String sportType) {
        goalRepository.deleteBySportType(sportType);
    }

}
