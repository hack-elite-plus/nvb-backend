package com.example.mygoal.service;

import org.springframework.stereotype.Service;

@Service
public class RunningValueService {
    private int currentValue = 0;

    public int getCurrentValue() {
        return currentValue;
    }
    private boolean paused = false;

    public void updateValue(int newValue) {
        currentValue = 0;
        while (currentValue < newValue) {
            if (!paused) {
                currentValue++;
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