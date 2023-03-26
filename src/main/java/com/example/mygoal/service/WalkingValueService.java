package com.example.mygoal.service;

import org.springframework.stereotype.Service;

@Service
public class WalkingValueService {
    private int walkingCurrentValue = 0;

    public int getCurrentValue() {
        return walkingCurrentValue;
    }
    private boolean paused = false;

    public void updateValue(int newValue) {
        walkingCurrentValue = 0;
        while (walkingCurrentValue < newValue) {
            if (!paused) {
                walkingCurrentValue++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void pauseCounting() {
        paused = true;
    }

    public void resumeCounting() {
        paused = false;
    }
}
