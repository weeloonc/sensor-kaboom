package libs;

import java.util.Arrays;
import java.lang.Math;
import java.util.LinkedList;
import java.util.Queue;
public abstract class BasicStatistics
{
    private final Queue<Float> queue = new LinkedList<Float>();
    private float lastValue;
    private int windowSize = 50;
    private int size;

    public void setWindowSize(int windowSize){
        this.windowSize = windowSize;
    }

    public void enQueue(float val){
        //System.out.println(val + " " + queue.size());
        if(this.queue.size() < windowSize){
            queue.offer(val);
        }else {
            queue.poll();
            queue.offer(val);
        }
        lastValue = val;
    }

    public float getLastValue(){
        return lastValue;
    }

    public float currentMagnitude(){
        return queue.peek();
    }

    public float getMean()
    {
        float sum = 0.0f;
        Float[] array = queue.toArray(new Float[queue.size()]);
        for(float a : array) {
            sum += a;
        }
        return sum/queue.size();
    }


    public float getVariance()
    {
        float mean = getMean();
        float temp = 0.0f;
        Float[] array = queue.toArray(new Float[queue.size()]);
        for(float a : array) {
            temp += (mean - a) * (mean - a);
        }
        return temp/queue.size();
    }

    public float getStdDev()
    {
        return (float)Math.sqrt(getVariance());
    }

    /*public float median()
    {
       Arrays.sort(data);

       if (data.length % 2 == 0)
       {
          return (float)(data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
       }
       else
       {
          return data[data.length / 2];
       }
    }*/
}