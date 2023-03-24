package com.example.mygoal.controller;

import com.example.mygoal.service.BikingValueService;
import com.example.mygoal.service.RunningValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
public class BikingValueController {
    @Autowired
    private BikingValueService bikingValueService;

    @PostMapping("/biking-update-value")
    public void updateValue(@RequestBody Map<String, Object> payload) {
        int newValue = Integer.parseInt(payload.get("value").toString());
        bikingValueService.updateValue(newValue);
    }
    @GetMapping("/biking-get-value")
    public Map<String, Integer> getValue() {
        Map<String, Integer> response = new HashMap<>();
        response.put("value", bikingValueService.getCurrentValue());
        return response;
    }
    @PostMapping("/biking-pause-counting")
    public void pauseCounting() {
        bikingValueService.pauseCounting();
    }
    @PostMapping("/biking-resume-counting")
    public void resumeCounting() {
        bikingValueService.resumeCounting();
    }
}
