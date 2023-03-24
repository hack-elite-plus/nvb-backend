package com.example.mygoal.controller;

import com.example.mygoal.model.Goal;
import com.example.mygoal.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/goal")

public class GoalController {
    @Autowired
    private GoalService goalService;

    @PostMapping("/add")
    public String add(@RequestBody Goal goal){
        goalService.saveUnique(goal.getSportType(), goal.getGoalType(), goal.getTimeframe(), goal.getValue());
        return "new goal is added";
    }

    @GetMapping("/getAll")
    public List<Goal> getAllGaol(){
        return goalService.getAllGoal();
    }

//    @GetMapping("/{id}/value")
//    public String getGoalValue(@PathVariable int id){
//         return goalService.getGoalValue(id);
//    }
    @GetMapping("/{sportType}/value")
    public String getGoalValue(@PathVariable String sportType){
        return goalService.getGoalValue(sportType);
    }

//    @DeleteMapping("/{id}/delete")
//    public String deleteGoal(@PathVariable int id){
//        return goalService.deleteGoal(id);
//    }

    @DeleteMapping("/{sportType}")
    public ResponseEntity<Void> deleteGoal(@PathVariable String sportType){
        goalService.deleteBySportType(sportType);
        return ResponseEntity.noContent().build();
    }
}
