package nus.cs4222.activitysim;

import java.io.*;
import java.util.*;
import java.text.*;

import android.hardware.*;
import android.util.*;

/**
   Class containing the activity detection algorithm.

   <p> You can code your activity detection algorithm in this class.
    (You may add more Java class files or add libraries in the 'libs' 
     folder if you need).
    The different callbacks are invoked as per the sensor log files, 
    in the increasing order of timestamps. In the best case, you will
    simply need to copy paste this class file (and any supporting class
    files and libraries) to the Android app without modification
    (in stage 2 of the project).

   <p> Remember that your detection algorithm executes as the sensor data arrives
    one by one. Once you have detected the user's current activity, output
    it using the {@link ActivitySimulator.outputDetectedActivity(UserActivities)}
    method. If the detected activity changes later on, then you need to output the
    newly detected activity using the same method, and so on.
    The detected activities are logged to the file "DetectedActivities.txt",
    in the same folder as your sensor logs.

   <p> To get the current simulator time, use the method
    {@link ActivitySimulator.currentTimeMillis()}. You can set timers using
    the {@link SimulatorTimer} class if you require. You can log to the 
    console/DDMS using either {@code System.out.println()} or using the
    {@link android.util.Log} class. You can use the {@code SensorManager.getRotationMatrix()}
    method (and any other helpful methods) as you would normally do on Android.

   <p> Note: Since this is a simulator, DO NOT create threads, DO NOT sleep(),
    or do anything that can cause the simulator to stall/pause. You 
    can however use timers if you require, see the documentation of the 
    {@link SimulatorTimer} class. 
    In the simulator, the timers are faked. When you copy the code into an
    actual Android app, the timers are real, but the code of this class
    does not need not be modified.
 */
public class ActivityDetection {

    /** 
       Called when the accelerometer sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Accl x value (m/sec^2)
       @param   y            Accl y value (m/sec^2)
       @param   z            Accl z value (m/sec^2)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onAcclSensorChanged( long timestamp , 
                                     float x , 
                                     float y , 
                                     float z , 
                                     int accuracy ) {

        // Process the sensor data as they arrive in each callback, 
        //  with all the processing in the callback itself (don't create threads).

        // You will most likely not need to use Timers at all, it is just 
        //  provided for convenience if you require.

    }

    /** 
       Called when the gravity sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Gravity x value (m/sec^2)
       @param   y            Gravity y value (m/sec^2)
       @param   z            Gravity z value (m/sec^2)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onGravitySensorChanged( long timestamp , 
                                        float x , 
                                        float y , 
                                        float z , 
                                        int accuracy ) {
    }

    /** 
       Called when the linear accelerometer sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Linear Accl x value (m/sec^2)
       @param   y            Linear Accl y value (m/sec^2)
       @param   z            Linear Accl z value (m/sec^2)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onLinearAcclSensorChanged( long timestamp , 
                                           float x , 
                                           float y , 
                                           float z , 
                                           int accuracy ) {
        
        double valueRaw = getMagnitude(x, y, z);
        
        EventWindow sWindow = sEventWindows.get(Sensor.TYPE_LINEAR_ACCELERATION);
        double value = sWindow.pushValue(valueRaw).getMean();
        
        EventWindow xlWindow = xlEventWindows.get(Sensor.TYPE_LINEAR_ACCELERATION);
        double stdDev = xlWindow.pushValue(valueRaw).getStdDevP();
        
        if (value < 1.5) {
            oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_LOW);
        }
        else if (stdDev < 1.0) {
            oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_MID);
        }
        else {
            oracle.setSensorActivity(Sensor.TYPE_LINEAR_ACCELERATION, ActivityOracle.SENSOR_ACTIVITY_HIGH);
        }

        oracle.pushActivityState(oracle.evaluateUserActivity());
        UserActivities newState = oracle.predictActivityState();
        
        if (newState != UserActivities.INCORRECT) {
            ActivitySimulator.outputDetectedActivity(newState);
        }
        //System.out.println(totalRaw + "\t" + stdDev);
    }

    /** 
       Called when the magnetic sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Magnetic x value (microTesla)
       @param   y            Magnetic y value (microTesla)
       @param   z            Magnetic z value (microTesla)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onMagneticSensorChanged( long timestamp , 
                                         float x , 
                                         float y , 
                                         float z , 
                                         int accuracy ) {

        double valueRaw = getMagnitude(x, y, z);
        
        EventWindow sWindow = sEventWindows.get(Sensor.TYPE_MAGNETIC_FIELD);
        double value = sWindow.pushValue(valueRaw).getMean();
        
        //EventWindow xlWindow = xlEventWindows.get(Sensor.TYPE_MAGNETIC_FIELD);
        double stdDev = xxlMagEventWindow.pushValue(valueRaw).getStdDevP();
        
        if (stdDev < 0.5) {
            oracle.setSensorActivity(Sensor.TYPE_MAGNETIC_FIELD, ActivityOracle.SENSOR_ACTIVITY_LOW);
        }
        else if (stdDev < 2.0) {
            oracle.setSensorActivity(Sensor.TYPE_MAGNETIC_FIELD, ActivityOracle.SENSOR_ACTIVITY_MID);
        }
        else {
            oracle.setSensorActivity(Sensor.TYPE_MAGNETIC_FIELD, ActivityOracle.SENSOR_ACTIVITY_HIGH);
        }
        
        //UserActivities newState = oracle.predictActivityState();
        
        //if (newState != UserActivities.INCORRECT) {
        //    ActivitySimulator.outputDetectedActivity(newState);
        //}
        
        //System.out.println(totalRaw + "\t" + stdDev);
    }

    /** 
       Called when the gyroscope sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Gyroscope x value (rad/sec)
       @param   y            Gyroscope y value (rad/sec)
       @param   z            Gyroscope z value (rad/sec)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onGyroscopeSensorChanged( long timestamp , 
                                          float x , 
                                          float y , 
                                          float z , 
                                          int accuracy ) {
    }

    /** 
       Called when the rotation vector sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Rotation vector x value (unitless)
       @param   y            Rotation vector y value (unitless)
       @param   z            Rotation vector z value (unitless)
       @param   scalar       Rotation vector scalar value (unitless)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onRotationVectorSensorChanged( long timestamp , 
                                               float x , 
                                               float y , 
                                               float z , 
                                               float scalar ,
                                               int accuracy ) {
    }

    /** 
       Called when the barometer sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   pressure     Barometer pressure value (millibar)
       @param   altitude     Barometer altitude value w.r.t. standard sea level reference (meters)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onBarometerSensorChanged( long timestamp , 
                                          float pressure , 
                                          float altitude , 
                                          int accuracy ) {
    }

    /** 
       Called when the light sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   light        Light value (lux)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onLightSensorChanged( long timestamp , 
                                      float light , 
                                      int accuracy ) {
    }

    /** 
       Called when the proximity sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   proximity    Proximity value (cm)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onProximitySensorChanged( long timestamp , 
                                          float proximity , 
                                          int accuracy ) {
    }

    /** 
       Called when the location sensor has changed.

       @param   timestamp    Timestamp of this location event
       @param   provider     "gps" or "network"
       @param   latitude     Latitude (deg)
       @param   longitude    Longitude (deg)
       @param   accuracy     Accuracy of the location data (you may use this) (meters)
       @param   altitude     Altitude (meters) (may be -1 if unavailable)
       @param   bearing      Bearing (deg) (may be -1 if unavailable)
       @param   speed        Speed (m/sec) (may be -1 if unavailable)
     */
    public void onLocationSensorChanged( long timestamp , 
                                         String provider , 
                                         double latitude , 
                                         double longitude , 
                                         float accuracy , 
                                         double altitude , 
                                         float bearing , 
                                         float speed ) {
    }

    /** Helper method to convert UNIX millis time into a human-readable string. */
    private static String convertUnixTimeToReadableString( long millisec ) {
        return sdf.format( new Date( millisec ) );
    }

    /** To format the UNIX millis time as a human-readable string. */
    private static final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd-h-mm-ssa" );

    private ActivityOracle oracle; 
    private Map<Integer, EventWindow> sEventWindows;
    private Map<Integer, EventWindow> xlEventWindows;
    private EventWindow xxlMagEventWindow;

    public ActivityDetection() {
        
        oracle = new ActivityOracle(1200); // stores past 30 sec activity for eval
        
        sEventWindows = new HashMap<Integer, EventWindow>();
        sEventWindows.put(Sensor.TYPE_LINEAR_ACCELERATION, new EventWindow(EventWindow.WINDOW_SIZE_SMALL));
        sEventWindows.put(Sensor.TYPE_MAGNETIC_FIELD, new EventWindow(EventWindow.WINDOW_SIZE_SMALL));
        
        xlEventWindows = new HashMap<Integer, EventWindow>();
        xlEventWindows.put(Sensor.TYPE_LINEAR_ACCELERATION, new EventWindow(EventWindow.WINDOW_SIZE_XLARGE));
        xlEventWindows.put(Sensor.TYPE_MAGNETIC_FIELD, new EventWindow(EventWindow.WINDOW_SIZE_XLARGE));
        
        xxlMagEventWindow = new EventWindow(80);
    }

    private double getMagnitude(double... tuple) {
        
        double sumOfSquares = 0.0;
        
        for (double value : tuple) {
            sumOfSquares += value * value;
        }
        
        return Math.sqrt(sumOfSquares);
    }

    public class EventWindow {
        
        public static final int WINDOW_SIZE_SMALL = 5;
        public static final int WINDOW_SIZE_MEDIUM = 10;
        public static final int WINDOW_SIZE_LARGE = 20;
        public static final int WINDOW_SIZE_XLARGE = 40;
        
        private double[] window;
        private double total;
        private int index;
        private int count;
        
        public EventWindow(int size) {
            window = new double[size];
        }
        
        public EventWindow pushValue(double value) {
            
            total = total - window[index] + value;
            window[index] = value;
            
            if (count < window.length) {
                count++;
            }
            
            index = ++index % window.length;
            
            return this;
        }
        
        public double getMean() {
            double mean = total / count;
            return mean;
        }
        
        public double getStdDevP() {
            
            double mean = getMean();
            double diff = 0.0;
            double diffSq = 0.0;
            double diffSqSum = 0.0;
            
            for (int i = 0; i < count; i++) {
                diff = window[i] - mean;
                diffSq = diff * diff;
                diffSqSum += diffSq;
            }
            
            double stdDev = Math.sqrt(diffSqSum / count);
            
            return stdDev;
        }
        
    }

    public class ActivityOracle {
        
        public static final int SENSOR_ACTIVITY_LOW = 0;
        public static final int SENSOR_ACTIVITY_MID = 1;
        public static final int SENSOR_ACTIVITY_HIGH = 2;
        
        private UserActivities[] window;
        private int index;
        private int stateCount;
        private int idleCount;
        private int walkingCount;
        private int vehicleCount;
        private int otherCount;
        
        private Map<Integer, Integer> sensorActivities;
        
        private int confidenceThreshold;
        private FileLogger log;
        
        public ActivityOracle(int windowSize) {
            sensorActivities = new HashMap<Integer, Integer>();
            sensorActivities.put(Sensor.TYPE_LINEAR_ACCELERATION, 0);
            sensorActivities.put(Sensor.TYPE_MAGNETIC_FIELD, 0);
            window = new UserActivities[windowSize];
            confidenceThreshold = (int) (0.85 * windowSize);
            log = new FileLogger();
            try {
                log.openLogFile(new File("sensor-logs/"), new Date().getTime() + "_oracle.csv");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        public void setSensorActivity(int sensorType, int sensorActivity) {
            sensorActivities.put(sensorType, sensorActivity);
        }
        
        public UserActivities evaluateUserActivity() {
            
            boolean isIdle = sensorActivities.get(Sensor.TYPE_LINEAR_ACCELERATION) <= SENSOR_ACTIVITY_MID
                    && sensorActivities.get(Sensor.TYPE_MAGNETIC_FIELD) == SENSOR_ACTIVITY_LOW;
            
            boolean isWalking = sensorActivities.get(Sensor.TYPE_LINEAR_ACCELERATION) == SENSOR_ACTIVITY_HIGH;
            
            boolean isVehicle = sensorActivities.get(Sensor.TYPE_LINEAR_ACCELERATION) <= SENSOR_ACTIVITY_MID
                    && sensorActivities.get(Sensor.TYPE_MAGNETIC_FIELD) >= SENSOR_ACTIVITY_MID;
            
            if (isIdle) {
                return UserActivities.IDLE_INDOOR;
            }
            else if (isWalking) {
                return UserActivities.WALKING;
            }
            else if (isVehicle) {
                return UserActivities.CAR;
            }
            else {
                return UserActivities.OTHER;
            }
        }
        
        public void pushActivityState(UserActivities state) {
            
            UserActivities purgedState = null;
            
            if (stateCount < window.length) {
                stateCount++;
            }
            else {
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
            
            log.logEvent(idleCount + "," + walkingCount + "," + vehicleCount);
        }
        
        public UserActivities predictActivityState() {
            
            if (idleCount > confidenceThreshold) {
                return UserActivities.IDLE_INDOOR;
            }
            else if (walkingCount > confidenceThreshold) {
                return UserActivities.WALKING;
            }
            else if (vehicleCount > confidenceThreshold) {
                return UserActivities.CAR;
            }
            else if (otherCount > confidenceThreshold) {
                return UserActivities.OTHER;
            }
            else { // no confidence to predict
                return UserActivities.INCORRECT;
            }
        }
        
    }

    public class HighPass {
        
        private double alpha;
        private double filteredValue;
        private double lastValue;
        
        public HighPass(double alpha) {
            this.alpha = alpha;
        }
        
        public HighPass pushValue(double value) {
            
            filteredValue = alpha * (filteredValue + value - lastValue);
            lastValue = value;
            
            return this;
        }
        
        public double getValue() {
            return filteredValue;
        }
        
    }

}
