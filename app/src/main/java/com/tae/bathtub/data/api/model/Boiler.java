package com.tae.bathtub.data.api.model;

import javax.inject.Inject;

/**
 * Created by Eduardo on 18/04/2016.
 */
public class Boiler {


    private Integer hot_water;
    private Integer cold_water;

    public Boiler(Integer hot_water, Integer cold_water) {
        this.hot_water = hot_water;
        this.cold_water = cold_water;
    }

    public int getHot_water() {
        return hot_water;
    }

    public int getCold_water() {
        return cold_water;
    }
}
