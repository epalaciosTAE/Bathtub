package com.tae.bathtub.data.local;

import java.util.List;

/**
 * Created by Eduardo on 19/04/2016.
 */
public class Bathtub {

    public static final int MAX_CAPACITY = 150;
    private int level;
    private int temperature;
    private List<Tap> taps;

    public List<Tap> getTaps() {
        return taps;
    }

    public void setTaps(List<Tap> taps) {
        this.taps = taps;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public boolean areTwoTapsOpen(List<Tap> tapList) {
        int counter = 0;
        for (Tap tap : tapList) {
            if (tap.isOpen()) {
                counter++;
            }
        }
        return counter > 1;
    }

    public int getTemperatureFromTaps(int cold, int hot) {
        return (cold + hot) / 2;
    }


}
