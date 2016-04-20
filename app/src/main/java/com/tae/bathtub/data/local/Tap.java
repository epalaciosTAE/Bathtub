package com.tae.bathtub.data.local;

/**
 * Created by Eduardo on 19/04/2016.
 */
public class Tap {

    public enum Type{
        COLD, HOT
    }

    private String type;
    private int temperature;
    private boolean open;

    public Tap () {
    }

    public Tap(String type, int temperature, boolean open) {
        this.type = type;
        this.temperature = temperature;
        this.open = open;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

}
