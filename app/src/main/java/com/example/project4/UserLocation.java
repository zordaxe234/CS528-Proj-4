package com.example.project4;

import android.location.Location;

public class UserLocation {
    public static final double LOCATION_CHANGE_RANGE = 10; //distance to move before new textview shows a new location in meters

    private Location originLocation;
    private float currentSensorValue;
    private float sensorValuesSummed = 0.0f;
    private int sensorSumCounter = 0;

    UserLocation(Location originLocation) {
        this.originLocation = originLocation;
    }

    /**
     * 0 - lat
     * 1 - lon
     * 2 - alt
     * 3 - locality
     * 4 - average
     * 5 - current (only for top)
     *
     * @param sensorValue
     */
    public void addToAverage(float sensorValue) {
        currentSensorValue = sensorValue;
        sensorValuesSummed += sensorValue;
        sensorSumCounter++;
    }

    public void resetSensors() {
        sensorSumCounter = 0;
        sensorValuesSummed = 0;
    }

    public double getAverage() {
        return sensorValuesSummed / sensorSumCounter;
    }

    public Location getOriginLocation() {
        return originLocation;
    }

    public float getCurrentSensorValue() {
        return currentSensorValue;
    }

    public void setCurrentSensorValue(float currentSensorValue) {
        this.currentSensorValue = currentSensorValue;
    }
}