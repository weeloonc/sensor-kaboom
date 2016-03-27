package libs;

/**
 * Created by Ryanzzz on 26/3/2016.
 */
public class magStatistics extends BasicStatistics {

    private float goodWalkingDev = 14f;//1.78f;
    private float goodWalkingMean = 50f;//2.5f;
   // private float goodWalkingMagnitude = 4.2f;

    private float goodBusDev = 8f;//0.67f;
    private float goodBusMean = 54f;//0.9f;
   // private float goodBusMagnitude = 2.0f;

    private float goodIdleDev = 4.0f;
    private float goodIdleMean = 45f;
   // private float goodIdleMagnitude = 0.3f;

    public magStatistics(int windowSize){
        super.setWindowSize(windowSize);
    }

    public float getWalkingProbability(){
        float currentDev = super.getStdDev();
        if (currentDev >= goodWalkingDev*2)
            currentDev = 0.000000001f;
        return currentDev/goodWalkingDev;
    }

    public float getBusProbability(){
        float currentDev = super.getStdDev();
        if (currentDev >= goodBusDev*2)
            currentDev = 0.000000001f;
        return currentDev/goodBusDev;
    }

    public float getIdleProbability(){
        float currentDev = super.getStdDev();
        if (currentDev >= goodIdleDev*2)
            currentDev = 0.000000001f;
        return currentDev/goodIdleDev;
    }
}
