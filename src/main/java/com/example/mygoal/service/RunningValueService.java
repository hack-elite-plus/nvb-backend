package com.example.mygoal.service;

import org.springframework.stereotype.Service;

@Service
public class RunningValueService {
    private int runningCurrentValue = 0;

    public int getCurrentValue() {
        return runningCurrentValue;
    }
    private boolean paused = false;

    public void updateValue(int newValue) {
        runningCurrentValue = 0;
        while (runningCurrentValue < newValue) {
            if (!paused) {
                runningCurrentValue++;
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