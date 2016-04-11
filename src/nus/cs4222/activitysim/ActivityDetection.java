package nus.cs4222.activitysim;

import java.util.*;

import android.hardware.*;
import android.location.*;
import nus.cs4222.activitysim.detector.*;

/**
 * Class containing the activity detection algorithm.
 * 
 * <p>
 * You can code your activity detection algorithm in this class. (You may add
 * more Java class files or add libraries in the 'libs' folder if you need). The
 * different callbacks are invoked as per the sensor log files, in the
 * increasing order of timestamps. In the best case, you will simply need to
 * copy paste this class file (and any supporting class files and libraries) to
 * the Android app without modification (in stage 2 of the project).
 * 
 * <p>
 * Remember that your detection algorithm executes as the sensor data arrives
 * one by one. Once you have detected the user's current activity, output it
 * using the {@link ActivitySimulator.outputDetectedActivity(UserActivities)}
 * method. If the detected activity changes later on, then you need to output
 * the newly detected activity using the same method, and so on. The detected
 * activities are logged to the file "DetectedActivities.txt", in the same
 * folder as your sensor logs.
 * 
 * <p>
 * To get the current simulator time, use the method
 * {@link ActivitySimulator.currentTimeMillis()}. You can set timers using the
 * {@link SimulatorTimer} class if you require. You can log to the console/DDMS
 * using either {@code System.out.println()} or using the
 * {@link android.util.Log} class. You can use the
 * {@code SensorManager.getRotationMatrix()} method (and any other helpful
 * methods) as you would normally do on Android.
 * 
 * <p>
 * Note: Since this is a simulator, DO NOT create threads, DO NOT sleep(), or do
 * anything that can cause the simulator to stall/pause. You can however use
 * timers if you require, see the documentation of the {@link SimulatorTimer}
 * class. In the simulator, the timers are faked. When you copy the code into an
 * actual Android app, the timers are real, but the code of this class does not
 * need not be modified.
 */
public class ActivityDetection {

    /** Initialises the detection algorithm. */
    public void initDetection() 
        throws Exception {
        // Add initialisation code here, if any

        // Here, we just show a dummy example of a timer that runs every 10 min, 
        //  outputting WALKING and INDOOR alternatively.
        // You will most likely not need to use Timers at all, it is just 
        //  provided for convenience if you require.
        // REMOVE THIS DUMMY CODE (2 lines below), otherwise it will mess up your algorithm's output
       /* SimulatorTimer timer = new SimulatorTimer();
        timer.schedule( this.task ,        // Task to be executed
                        10 * 60 * 1000 );  // Delay in millisec (10 min)*/
    }

    /** De-initialises the detection algorithm. */
    public void deinitDetection() 
        throws Exception {
        // Add de-initialisation code here, if any
    }
    
    /**
     * Called when the accelerometer sensor has changed.
     * 
     * @param timestamp
     *            Timestamp of this sensor event
     * @param x
     *            Accl x value (m/sec^2)
     * @param y
     *            Accl y value (m/sec^2)
     * @param z
     *            Accl z value (m/sec^2)
     * @param accuracy
     *            Accuracy of the sensor data (you can ignore this)
     */
    public void onAcclSensorChanged(long timestamp, float x, float y, float z, int accuracy) {

    }

    /**
     * Called when the gravity sensor has changed.
     * 
     * @param timestamp
     *            Timestamp of this sensor event
     * @param x
     *            Gravity x value (m/sec^2)
     * @param y
     *            Gravity y value (m/sec^2)
     * @param z
     *            Gravity z value (m/sec^2)
     * @param accuracy
     *            Accuracy of the sensor data (you can ignore this)
     */
    public void onGravitySensorChanged(long timestamp, float x, float y, float z, int accuracy) {

    }

    /**
     * Called when the linear accelerometer sensor has changed.
     * 
     * @param timestamp
     *            Timestamp of this sensor event
     * @param x
     *            Linear Accl x value (m/sec^2)
     * @param y
     *            Linear Accl y value (m/sec^2)
     * @param z
     *            Linear Accl z value (m/sec^2)
     * @param accuracy
     *            Accuracy of the sensor data (you can ignore this)
     */
    public void onLinearAcclSensorChanged(long timestamp, float x, float y, float z, int accuracy) {

        double valueRaw = getMagnitude(x, y, z);
        double value = linAcclWindowMean.pushValue(valueRaw).getMean();
        double stdDev = linAcclWindowStdDev.pushValue(valueRaw).getStdDevP();

       /* if (value < 1.2) {
            oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_LOW);
        } else if (stdDev < 0.9) {
            oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_MID);
        } else {
            oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_HIGH);
        }*/
        if (value < 1.1) {
            if(stdDev > 0.4)
                oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_MID);
            else
                oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_LOW);
        }else if ( stdDev < 0.8) {
            oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_MID);
        } else {
            oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_HIGH);
        }
        /*if (value < 0.9 && stdDev < 0.5) {
            oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_LOW);
        } else if (value < 1.1 && stdDev < 0.9) {
            oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_MID);
        } *//*else if (value > 1 && stdDev > 0.7) {
            oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_MID);
        }*//* else {
            oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_HIGH);
        }*/

        if (outputCoordinator++ == 0) {
            UserActivities currentState = oracle.evaluateUserActivity();
            oracle.pushActivityState(currentState);
            ioOracle.pushActivityState(ioOracle.ioEvaluator());

            UserActivities predictedState = oracle.predictActivityState();
            outputCoordinator %= rateDivisor;

            if (predictedState != UserActivities.INCORRECT) {
                ActivitySimulator.outputDetectedActivity(predictedState);
            }
        }
    }

    /**
     * Called when the magnetic sensor has changed.
     * 
     * @param timestamp
     *            Timestamp of this sensor event
     * @param x
     *            Magnetic x value (microTesla)
     * @param y
     *            Magnetic y value (microTesla)
     * @param z
     *            Magnetic z value (microTesla)
     * @param accuracy
     *            Accuracy of the sensor data (you can ignore this)
     */
    public void onMagneticSensorChanged(long timestamp, float x, float y, float z, int accuracy) {

        double valueRaw = getMagnitude(x, y, z);
        double stdDev = magWindow.pushValue(valueRaw).getStdDevP();

        if (stdDev < 0.8) {
            oracle.setSensorActivity(Sensor.TYPE_MAGNETIC_FIELD, ActivityOracle.SENSOR_ACTIVITY_LOW);
        } else if (stdDev < 1.9) {
            oracle.setSensorActivity(Sensor.TYPE_MAGNETIC_FIELD, ActivityOracle.SENSOR_ACTIVITY_MID);
        } else {
            oracle.setSensorActivity(Sensor.TYPE_MAGNETIC_FIELD, ActivityOracle.SENSOR_ACTIVITY_HIGH);
        }

        // Light detector
        if (stdDev <= 0.7) {
            ioOracle.setLightActivity(Sensor.TYPE_MAGNETIC_FIELD, IdleActivityOracle.LIGHTSENSOR_ACTIVITY_LOW);
        } else {
            ioOracle.setLightActivity(Sensor.TYPE_MAGNETIC_FIELD, IdleActivityOracle.LIGHTSENSOR_ACTIVITY_HIGH);
        }
    }

    /**
     * Called when the gyroscope sensor has changed.
     * 
     * @param timestamp
     *            Timestamp of this sensor event
     * @param x
     *            Gyroscope x value (rad/sec)
     * @param y
     *            Gyroscope y value (rad/sec)
     * @param z
     *            Gyroscope z value (rad/sec)
     * @param accuracy
     *            Accuracy of the sensor data (you can ignore this)
     */
    public void onGyroscopeSensorChanged(long timestamp, float x, float y, float z, int accuracy) {

    }

    /**
     * Called when the rotation vector sensor has changed.
     * 
     * @param timestamp
     *            Timestamp of this sensor event
     * @param x
     *            Rotation vector x value (unitless)
     * @param y
     *            Rotation vector y value (unitless)
     * @param z
     *            Rotation vector z value (unitless)
     * @param scalar
     *            Rotation vector scalar value (unitless)
     * @param accuracy
     *            Accuracy of the sensor data (you can ignore this)
     */
    public void onRotationVectorSensorChanged(long timestamp, float x, float y, float z, float scalar, int accuracy) {

    }

    /**
     * Called when the barometer sensor has changed.
     * 
     * @param timestamp
     *            Timestamp of this sensor event
     * @param pressure
     *            Barometer pressure value (millibar)
     * @param altitude
     *            Barometer altitude value w.r.t. standard sea level reference
     *            (meters)
     * @param accuracy
     *            Accuracy of the sensor data (you can ignore this)
     */
    public void onBarometerSensorChanged(long timestamp, float pressure, float altitude, int accuracy) {

    }

    /**
     * Called when the light sensor has changed.
     * 
     * @param timestamp
     *            Timestamp of this sensor event
     * @param light
     *            Light value (lux)
     * @param accuracy
     *            Accuracy of the sensor data (you can ignore this)
     */
    public void onLightSensorChanged(long timestamp, float light, int accuracy) {

        double value = lightWindow.pushValue(light).getMean();

        if (value < 1000.0) {
            ioOracle.setLightActivity(Sensor.TYPE_LIGHT, IdleActivityOracle.LIGHTSENSOR_ACTIVITY_LOW);
        } else {
            ioOracle.setLightActivity(Sensor.TYPE_LIGHT, IdleActivityOracle.LIGHTSENSOR_ACTIVITY_HIGH);
        }
    }

    /**
     * Called when the proximity sensor has changed.
     * 
     * @param timestamp
     *            Timestamp of this sensor event
     * @param proximity
     *            Proximity value (cm)
     * @param accuracy
     *            Accuracy of the sensor data (you can ignore this)
     */
    public void onProximitySensorChanged(long timestamp, float proximity, int accuracy) {

    }

    /**
     * Called when the location sensor has changed.
     * 
     * @param timestamp
     *            Timestamp of this location event
     * @param provider
     *            "gps" or "network"
     * @param latitude
     *            Latitude (deg)
     * @param longitude
     *            Longitude (deg)
     * @param accuracy
     *            Accuracy of the location data (you may use this) (meters)
     * @param altitude
     *            Altitude (meters) (may be -1 if unavailable)
     * @param bearing
     *            Bearing (deg) (may be -1 if unavailable)
     * @param speed
     *            Speed (m/sec) (may be -1 if unavailable)
     */
    public void onLocationSensorChanged(long timestamp, String provider, double latitude, double longitude,
            float accuracy, double altitude, float bearing, float speed) {

        double mean;
        
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            oracle.setGpsProvider(true);
            mean = locWindow.pushValue(speed).getMean();
        } else {
            oracle.setGpsProvider(false);
            mean = locWindow.pushValue(0).getMean();
        }

        double speedInKmh = getSpeedInKmh(mean);

       // if (speedInKmh <= 1.08) {
        if (speedInKmh <= 0.7) {
            ioOracle.setLightActivity(LocationSensor.TYPE_LOCATION, LocationSensor.GPS_SPEED_LOW);
            oracle.setSensorActivity(LocationSensor.TYPE_LOCATION, ActivityOracle.SENSOR_ACTIVITY_LOW);
        } else if (speedInKmh <= 1.1) {
            ioOracle.setLightActivity(LocationSensor.TYPE_LOCATION, LocationSensor.GPS_SPEED_MID);
            oracle.setSensorActivity(LocationSensor.TYPE_LOCATION, ActivityOracle.SENSOR_ACTIVITY_MID);
        } else {
            ioOracle.setLightActivity(LocationSensor.TYPE_LOCATION, LocationSensor.GPS_SPEED_HIGH);
            oracle.setSensorActivity(LocationSensor.TYPE_LOCATION, ActivityOracle.SENSOR_ACTIVITY_HIGH);
        }
    }

    private ActivityOracle oracle;
    private IdleActivityOracle ioOracle;

    private EventWindow linAcclWindowMean;
    private EventWindow linAcclWindowStdDev;
    private EventWindow magWindow;
    private EventWindow lightWindow;
    private EventWindow locWindow;
    
    private int outputCoordinator = 0;
    private int rateDivisor = 1;

    public ActivityDetection() {

        oracle = new ActivityOracle(1195 / rateDivisor, 0.861);
        //ioOracle = new IdleActivityOracle(735, 0.677);
        ioOracle = new IdleActivityOracle(735 / rateDivisor, 0.677);

        oracle.setIdleActivityOracle(ioOracle);

        linAcclWindowMean = new EventWindow(60);
        linAcclWindowStdDev = new EventWindow(120);
        //magWindow = new EventWindow(70);
        magWindow = new EventWindow(70);
        lightWindow = new EventWindow(5);
        locWindow = new EventWindow(7);
    }

    private double getMagnitude(double... tuple) {

        double sumOfSquares = 0.0;

        for (double value : tuple) {
            sumOfSquares += value * value;
        }

        return Math.sqrt(sumOfSquares);
    }

    private double getSpeedInKmh(double speedInMs) {
        return speedInMs / 1000 * 60 * 60;
    }

}
