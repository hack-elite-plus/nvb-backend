package com.example.mygoal.service;

import org.springframework.stereotype.Service;

@Service
public class BikingValueService {
    private int bikingCurrentValue = 0;

    public int getCurrentValue() {
        return bikingCurrentValue;
    }
    private boolean paused = false;

    public void updateValue(int newValue) {
        bikingCurrentValue = 0;
        while (bikingCurrentValue < newValue) {
            if (!paused) {
                bikingCurrentValue++;
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
