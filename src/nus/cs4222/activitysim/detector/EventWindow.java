package nus.cs4222.activitysim.detector;

public class EventWindow {

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
