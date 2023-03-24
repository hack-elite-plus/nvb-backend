package com.example.mygoal.controller;

import com.example.mygoal.service.RunningValueService;
import com.example.mygoal.service.WalkingValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
public class WalkingValueController {
    @Autowired
    private WalkingValueService walkingValueService;

    @PostMapping("/walking-update-value")
    public void updateValue(@RequestBody Map<String, Object> payload) {
        int newValue = Integer.parseInt(payload.get("value").toString());
        walkingValueService.updateValue(newValue);
    }
    @GetMapping("/walking-get-value")
    public Map<String, Integer> getValue() {
        Map<String, Integer> response = new HashMap<>();
        response.put("value", walkingValueService.getCurrentValue());
        return response;
    }
    @PostMapping("/walking-pause-counting")
    public void pauseCounting() {
        walkingValueService.pauseCounting();
    }
    @PostMapping("/walking-resume-counting")
    public void resumeCounting() {
        walkingValueService.resumeCounting();
    }
}
