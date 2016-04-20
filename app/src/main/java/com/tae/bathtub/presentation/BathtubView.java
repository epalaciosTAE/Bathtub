package com.tae.bathtub.presentation;

/**
 * Created by Eduardo on 19/04/2016.
 */
public interface BathtubView {
    void waterLevelOverflow(boolean overflow);
    void increaseWaterLevel(float level);
    void displayTemperature(int temp);
    void setTemperatureIndicator(float indicatorPosition);
    void showToast(String message);
}
