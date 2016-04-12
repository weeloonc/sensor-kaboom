package nus.cs4222.activitysim.detector;

import java.util.HashMap;
import java.util.Map;

import android.hardware.Sensor;
import nus.cs4222.activitysim.UserActivities;

public class IdleActivityOracle {

    public static final int LIGHTSENSOR_ACTIVITY_LOW = 0;
    public static final int LIGHTSENSOR_ACTIVITY_MID = 1;
    public static final int LIGHTSENSOR_ACTIVITY_HIGH = 2;

    public static final int PROXIMITY_LOW = 0;
    public static final int PROXIMITY_HIGH = 2;

    public static final int GPS_SIGNAL_HIGH = 1;
    public static final int GPS_SIGNAL_LOW = 0;

    private UserActivities[] window;
    private Map<Integer, Integer> lightActivity;
    private int confidenceThreshold;

    private boolean usingGpsProvider;

    private int index;
    private int indoorCount;
    private int outdoorCount;
    private int otherCount;
    private int stateCount;

    public IdleActivityOracle(int windowSize, double confidencePercentage) {
        lightActivity = new HashMap<Integer, Integer>();
        lightActivity.put(Sensor.TYPE_LIGHT, 0);
        lightActivity.put(Sensor.TYPE_MAGNETIC_FIELD, 0);
        lightActivity.put(Sensor.TYPE_PROXIMITY, 0);
        lightActivity.put(LocationSensor.TYPE_LOCATION, 0);
        window = new UserActivities[windowSize];
        confidenceThreshold = (int) (confidencePercentage * windowSize);
    }

    public void setLightActivity(int sensorType, int value) {
        lightActivity.put(sensorType, value);
    }

    public void setGpsProvider(boolean usingGpsProvider) {
        this.usingGpsProvider = usingGpsProvider;
    }

    public UserActivities ioEvaluator() {
        boolean isIndoor, isOutdoor;
        // if(lightActivity.get(Sensor.TYPE_PROXIMITY) == PROXIMITY_HIGH){
      /*  isOutdoor = lightActivity.get(Sensor.TYPE_LIGHT) == LIGHTSENSOR_ACTIVITY_HIGH;
        if (isOutdoor && usingGpsProvider) {
            return UserActivities.IDLE_OUTDOOR;
        }else if (isOutdoor && lightActivity.get(Sensor.TYPE_MAGNETIC_FIELD) == LIGHTSENSOR_ACTIVITY_HIGH *//*&& usingGpsProvider*//*
                *//*&& lightActivity.get(LocationSensor.TYPE_LOCATION) == LocationSensor.GPS_SPEED_LOW*//*) {
            return UserActivities.IDLE_OUTDOOR;
        }*/

        /*if(!usingGpsProvider && lightActivity.get(Sensor.TYPE_PROXIMITY) == PROXIMITY_HIGH &&
                lightActivity.get(Sensor.TYPE_LIGHT) == LIGHTSENSOR_ACTIVITY_MID){
            return UserActivities.IDLE_INDOOR;
        } else if (lightActivity.get(Sensor.TYPE_MAGNETIC_FIELD) == LIGHTSENSOR_ACTIVITY_LOW *//*&& !usingGpsProvider*//*
                *//*&& lightActivity.get(LocationSensor.TYPE_LOCATION) == LocationSensor.GPS_SPEED_LOW*//*) {
            return UserActivities.IDLE_INDOOR;

        } else {
            return UserActivities.IDLE_OUTDOOR;
        }*/
        if(/*!usingGpsProvider && */lightActivity.get(Sensor.TYPE_PROXIMITY) == PROXIMITY_HIGH
                && lightActivity.get(Sensor.TYPE_LIGHT) <= LIGHTSENSOR_ACTIVITY_MID
                /*&& lightActivity.get(LocationSensor.TYPE_LOCATION) == LocationSensor.GPS_SPEED_MID*/){
            return UserActivities.IDLE_INDOOR;
        } else if (lightActivity.get(Sensor.TYPE_PROXIMITY) == PROXIMITY_LOW
                //&& lightActivity.get(Sensor.TYPE_MAGNETIC_FIELD) <= LIGHTSENSOR_ACTIVITY_MID
                //&& lightActivity.get(Sensor.TYPE_LIGHT) == LIGHTSENSOR_ACTIVITY_LOW/*&& !usingGpsProvider*/
                && lightActivity.get(LocationSensor.TYPE_LOCATION) == LocationSensor.GPS_SPEED_LOW) {
            return UserActivities.IDLE_INDOOR;

        } else {
            return UserActivities.IDLE_OUTDOOR;
        }
    }

    public void pushActivityState(UserActivities state) {

        UserActivities purgedState = null;

        if (stateCount < window.length) {
            stateCount++;
        } else {
            purgedState = window[index];
        }

        window[index] = state;
        index = ++index % window.length;

        if (purgedState != null) {
            switch (purgedState) {
            case IDLE_INDOOR:
                indoorCount--;
                break;
            case IDLE_OUTDOOR:
                outdoorCount--;
                break;
            case OTHER:
                otherCount--;
                break;
            default:
                throw new AssertionError("Should not happen");
            }
        }

        switch (state) {
        case IDLE_INDOOR:
            indoorCount++;
        case IDLE_OUTDOOR:
            outdoorCount++;
        case OTHER:
            otherCount++;
            break;
        default:
            throw new AssertionError("Should not happen");
        }
    }

    public UserActivities predictActivityState() {

        if (indoorCount > confidenceThreshold) {
            return UserActivities.IDLE_INDOOR;
        } else if (outdoorCount > confidenceThreshold) {
            return UserActivities.IDLE_OUTDOOR;
        } else { // no confidence to predict
            return UserActivities.INCORRECT;
        }
    }

    // on Proximity change, tells IODetector to read from LightSensor
    // if LightSensor > 1000, mag std dev < 1, output IDLE_Outdoor
    // if lightsensor < 1000, mag std dev > 1, output IDLE_Indoor

}
