package libs;
/**
 * Created by Ryanzzz on 25/3/2016.
 */
public class dataFilters {

    /*
     * time smoothing constant for low-pass filter
     * 0 ≤ alpha ≤ 1 ; a smaller value basically means more smoothing
     * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     */
    static final float ALPHA = 0.15f;

    public dataFilters(){

    }


    public float[] highPass(float[] input, float[] output) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = input[i] - (output[i] + ALPHA * (input[i] - output[i]));
        }

        return(output);
    }

    /**
     * @see http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
     * @see http://developer.android.com/reference/android/hardware/SensorEvent.html#values
     */

    public float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
