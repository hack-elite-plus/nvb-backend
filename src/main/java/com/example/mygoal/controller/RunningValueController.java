package com.example.mygoal.controller;

import com.example.mygoal.service.RunningValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
public class RunningValueController {
    @Autowired
    private RunningValueService runningValueService;

    @PostMapping("/running-update-value")
    public void updateValue(@RequestBody Map<String, Object> payload) {
        int newValue = Integer.parseInt(payload.get("value").toString());
        runningValueService.updateValue(newValue);
    }
    @GetMapping("/running-get-value")
    public Map<String, Integer> getValue() {
        Map<String, Integer> response = new HashMap<>();
        response.put("value", runningValueService.getCurrentValue());
        return response;
    }
    @PostMapping("/runing-pause-counting")
    public void pauseCounting() {
        runningValueService.pauseCounting();
    }
    @PostMapping("/running-resume-counting")
    public void resumeCounting() {
        runningValueService.resumeCounting();
    }

}
