package nus.cs4222.activitysim.detector;

import java.util.HashMap;
import java.util.Map;

import android.hardware.Sensor;
import nus.cs4222.activitysim.UserActivities;

public class ActivityOracle {

    public static final int SENSOR_ACTIVITY_LOW = 0;
    public static final int SENSOR_ACTIVITY_MID = 1;
    public static final int SENSOR_ACTIVITY_HIGH = 2;

    private UserActivities[] window;
    private Map<Integer, Integer> sensorActivities;
    private int confidenceThreshold;

    private boolean usingGpsProvider;

    private int index;
    private int stateCount;
    private int idleCount;
    private int walkingCount;
    private int vehicleCount;
    private int otherCount;

    private IdleActivityOracle ioOracle;

    public ActivityOracle(int windowSize, double confidencePercentage) {

        sensorActivities = new HashMap<Integer, Integer>();
        sensorActivities.put(Sensor.TYPE_LINEAR_ACCELERATION, 0);
        sensorActivities.put(Sensor.TYPE_MAGNETIC_FIELD, 0);
        sensorActivities.put(LocationSensor.TYPE_LOCATION, 0);

        window = new UserActivities[windowSize];
        confidenceThreshold = (int) (confidencePercentage * windowSize);
    }

    public void setIdleActivityOracle(IdleActivityOracle ioDetector) {
        this.ioOracle = ioDetector;
    }

    public void setGpsProvider(boolean usingGpsProvider) {
        this.usingGpsProvider = usingGpsProvider;
        ioOracle.setGpsProvider(usingGpsProvider);
    }

    public void setSensorActivity(int sensorType, int sensorActivity) {
        sensorActivities.put(sensorType, sensorActivity);
    }

    public UserActivities evaluateUserActivity() {

        boolean isIdle = sensorActivities.get(Sensor.TYPE_LINEAR_ACCELERATION) == SENSOR_ACTIVITY_LOW
                && sensorActivities.get(Sensor.TYPE_MAGNETIC_FIELD) == SENSOR_ACTIVITY_LOW
                && (usingGpsProvider ? sensorActivities.get(LocationSensor.TYPE_LOCATION) == SENSOR_ACTIVITY_LOW : true);

        boolean isWalking = sensorActivities.get(Sensor.TYPE_LINEAR_ACCELERATION) == SENSOR_ACTIVITY_HIGH
                && sensorActivities.get(Sensor.TYPE_MAGNETIC_FIELD) >= SENSOR_ACTIVITY_MID
                && sensorActivities.get(LocationSensor.TYPE_LOCATION) <= SENSOR_ACTIVITY_MID;

        boolean isVehicle = sensorActivities.get(Sensor.TYPE_LINEAR_ACCELERATION) <= SENSOR_ACTIVITY_MID
                && sensorActivities.get(Sensor.TYPE_MAGNETIC_FIELD) >= SENSOR_ACTIVITY_MID
                && (usingGpsProvider ? sensorActivities.get(LocationSensor.TYPE_LOCATION) >= SENSOR_ACTIVITY_MID : true)
                || (usingGpsProvider && sensorActivities.get(LocationSensor.TYPE_LOCATION) >= SENSOR_ACTIVITY_MID);

        if (isIdle) {
            return UserActivities.IDLE_INDOOR;
        } else if (isWalking) {
            return UserActivities.WALKING;
        } else if (isVehicle) {
            return UserActivities.CAR;
        } else {
            return UserActivities.WALKING;
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
            case IDLE_OUTDOOR:
                idleCount--;
                break;
            case WALKING:
                walkingCount--;
                break;
            case BUS:
            case CAR:
            case TRAIN:
                vehicleCount--;
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
        case IDLE_OUTDOOR:
            idleCount++;
            break;
        case WALKING:
            walkingCount++;
            break;
        case BUS:
        case CAR:
        case TRAIN:
            vehicleCount++;
            break;
        case OTHER:
            otherCount++;
            break;
        default:
            throw new AssertionError("Should not happen");
        }
    }

    public UserActivities predictActivityState() {

        if (idleCount > confidenceThreshold) {
            return ioOracle.predictActivityState();
        } else if (walkingCount > confidenceThreshold) {
            return UserActivities.WALKING;
        } else if (vehicleCount > confidenceThreshold) {
            return UserActivities.CAR;
        } else if (otherCount > confidenceThreshold) {
            return UserActivities.OTHER;
        } else { // no confidence to predict
            return UserActivities.INCORRECT;
        }
    }
}
