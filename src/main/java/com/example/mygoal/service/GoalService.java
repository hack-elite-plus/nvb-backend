package com.example.mygoal.service;

import com.example.mygoal.model.Goal;

import java.util.List;

public interface GoalService {
//   public Goal saveGoal(Goal goal);
   public List<Goal> getAllGoal();

//   public String getGoalValue(int id);
   public  String getGoalValue(String sportType);

   void saveUnique(String sportType, String goalType, String timeframe, String value);

   void deleteBySportType(String sportType);
//   public String deleteGoal(int id);
}
