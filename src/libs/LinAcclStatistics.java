package libs;

/**
 * Created by Ryanzzz on 25/3/2016.
 */
public class LinAcclStatistics extends BasicStatistics {

    private float goodWalkingDev = 1.2f;//2.6f;//1.78f;
    private float goodWalkingMean = 1.8f;//4.2f;//2.5f;
    private float goodWalkingMagnitude = 4.2f;

    private float goodBusDev = 0.5f;//0.67f;
    private float goodBusMean = 0.698f;//0.9f;
    private float goodBusMagnitude = 2.0f;

    private float goodIdleDev = 0.05f;
    private float goodIdleMean = 0.08f;
    private float goodIdleMagnitude = 0.3f;

    public LinAcclStatistics(int windowSize){
        super.setWindowSize(windowSize);
    }

    public float getWalkingProbability(){
        float currentMean = super.getMean() > goodWalkingMean ? goodWalkingMean : super.getMean();
        float currentDev = super.getStdDev() > goodWalkingDev ? goodWalkingDev : super.getStdDev();
        float currentMagnitude= super.getLastValue();
        //float walkingProbability = (currentMagnitude + currentMean + currentDev) / (goodWalkingMean + goodWalkingDev + goodWalkingMagnitude);
        //float walkingProbability = /*(currentMagnitude/goodWalkingMagnitude) **/ (currentMean/goodWalkingMean) * (currentDev/goodWalkingDev);
        float walkingProbability = Math.abs(currentMean-currentDev) / Math.abs(goodWalkingMean-goodWalkingDev) * currentMean/goodWalkingMean;
       /* float walkingProbability =0;
        if(currentMean > currentDev) {
            walkingProbability = (currentMean - currentDev) / (goodWalkingMean - goodWalkingDev);
        }
        else*/

        System.out.println();
        System.out.println("Walking, mag: " + currentMagnitude + " mean: " + currentMean + " dev: " +currentDev);
        System.out.println("Math.abs(" + currentMean + "-" + currentDev+ ") = " + Math.abs(currentMean-currentDev) +
                "/Math.abs(" + goodWalkingMean+ "-" + goodWalkingDev+ ") = " +  Math.abs(goodWalkingMean-goodWalkingDev) +
                " * " + currentMean + "/" + goodWalkingMean + " = " + walkingProbability);
        return walkingProbability;
    }

    public float getBusProbability(){
        float currentMean = super.getMean() ;
        float currentDev = super.getStdDev();
        float currentMagnitude= super.getLastValue();

        if (currentMagnitude >= goodBusMagnitude * 2.0f)     //if exceed twice the good amount it is not good indicator
            currentMagnitude = 0.01f;
        else if(currentMagnitude > goodBusMagnitude)
            currentMagnitude = goodBusMagnitude - (currentMagnitude - goodBusMagnitude);  // eg 1.9 mag will be 1.1mag as it exceeded by 0.4
      // System.out.println("current: " + currentMagnitude + " " + goodBusMagnitude + " after: " + (currentMagnitude - goodBusMagnitude));
        if(currentDev >= goodBusDev * 2)
            return 0.0f;
        else if(currentDev > goodBusDev)
            currentDev = goodBusDev - (currentDev - goodBusDev);
        if(currentDev < 0.1f)
            return 0.0f;

        if(currentMean > goodBusMean * 2)
            return 0.0f;
        else if(currentMean > goodBusMean)
            currentMean = goodBusMean - (currentMean - goodBusMean);

        if(currentMean > goodBusMean * 2)
            System.out.println("HIHI");
        if(currentMean <= goodBusMean * 2)
            System.out.println("LOLO");

        float busProbability = Math.abs(currentMean-currentDev) / 0.59999995f/*Math.abs(goodBusMean-goodBusDev)*/ * currentMean/goodBusMean;
        //float busProbability = /*(currentMagnitude/goodBusMagnitude) **/ (currentMean/goodBusMean) * (currentDev/goodBusDev);
        //float busProbability = (currentMagnitude + currentMean + currentDev) / (goodBusMean + goodBusDev + goodBusMagnitude);
        System.out.println("Bus, mag: " + currentMagnitude + " mean: " + currentMean + " dev: " +currentDev);
        System.out.println("Math.abs(" + currentMean + "-" + currentDev+ ") = " + Math.abs(currentMean-currentDev) +
                           "/Math.abs(" + goodBusMean+ "-" + goodBusDev+ ") = " +  Math.abs(goodBusMean-goodBusDev) +
                            " * " + currentMean + "/" + goodBusMean + " = " + busProbability);
        return busProbability;
    }

    public float getIdleProbability(){
        float currentMean = super.getMean() ;
        float currentDev = super.getStdDev();
        float currentMagnitude= super.getLastValue();


        if (currentMagnitude >= goodIdleMagnitude*2)
            currentMagnitude = 0.000000001f;
        if (currentMean >= goodIdleMean*2)
            currentMean = 0.000000001f;
        if (currentDev >= goodIdleDev*2)
            currentDev = 0.000000001f;
        float idleProbability = Math.abs(currentMean-currentDev) / Math.abs(goodIdleMean-goodIdleDev) * currentMean/goodIdleMean;
      //  float idleProbability = (currentMagnitude/goodIdleMagnitude) * (currentMean/goodIdleMean) * (currentDev/goodIdleDev);

        return idleProbability;
    }


}
