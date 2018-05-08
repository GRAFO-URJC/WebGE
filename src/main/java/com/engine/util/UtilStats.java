package com.engine.util;

import java.util.Properties;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * Utility methods for statistics.
 * 
 * @author J. M. Colmenar
 */
public class UtilStats {

    private static Double penaltyB;
    private static Double penaltyC;
    private static Double penaltyD;
    private static Double penaltyE;
    
    public static final double DEFAULT_CEG_PENALTY_B = 1.0;
    public static final double DEFAULT_CEG_PENALTY_C = 10.0;
    public static final double DEFAULT_CEG_PENALTY_D = 100.0;
    public static final double DEFAULT_CEG_PENALTY_E = 1000.0;
    

    /**
     * Computes the Root Mean Squared Error (RMSE) between two arrays of engine.data.
     * 
     * @param l1
     * @param l2
     * @return 
     */
    public static double computeRMSE(double[] l1, double[] l2) {
        double acu = 0.0;

        for (int i = 0; i < l1.length; i++) {
            acu += Math.pow(l1[i] - l2[i], 2);
        }

        return Math.sqrt(acu / l1.length);
    }
    
    public static double computeAvgError(double[] expected, double[] observed) {
        double error = 0.0;
        for (int k = 0; k < expected.length; ++k) {
            error += Math.abs(expected[k] - observed[k]);
        }
        error /= expected.length;
        return error;
    }    
    
    public static double getAverage(double[] darr) {
        return StatUtils.mean(darr);
    }
    
    public static double calculatePvalueChiSquare(double[] expectedIn, double[] observedIn) {
        double pValue = 0.0;
        
        double acu = 0.0;
        for (int i=0; i<expectedIn.length; i++) {
            acu += (Math.pow(expectedIn[i]-observedIn[i], 2)) / expectedIn[i];
        }
        
        ChiSquaredDistribution chiDist = new ChiSquaredDistribution((double) expectedIn.length-1);
        try {
            pValue = 1.0 - chiDist.cumulativeProbability(acu);
        } catch (Exception e) {
            System.err.println("Error calculating p-value: "+e.getLocalizedMessage());
            System.err.println("--> Accumulated value was: "+acu);
        }
        return pValue;
    }
    
    
    private static final int CLARKE_REGIONS = 5;
    // Zones for grids:
    private static final int A = 0;
    private static final int B = 1;
    private static final int C = 2;
    private static final int D = 3;
    private static final int E = 4;
    
    /**
     * Clarke error grid of the given point.
     *
     * @param y Reference glucose values
     * @param yp Estimated glucose values
     *
     * @return The key of the region where the point is places: A, B, C, D or E. 
     */
    public static int determineCEGRegion(double y, double yp) {
        if (((yp <= 70) && (y <= 70))
                || ((yp <= (1.2 * y)) && (yp >= (0.8 * y)))) {
            return A;            // Zone A
        } else {
            if (((y >= 180) && (yp <= 70))
                    || ((y <= 70) && yp >= 180)) {
                return E;        // Zone E
            } else {
                if ((((y >= 70) && (y <= 290)) && (yp >= (y + 110)))
                        || (((y >= 130) && (y <= 180)) && (yp <= (1.4 * y) - 182))) {   // 1.4 = 7/5
                    return C;    // Zone C
                } else {
                    if (((y >= 240) && ((yp >= 70) && (yp <= 180)))
                            || ((y <= (175 / 3)) && (yp <= 180) && (yp >= 70))
                            || (((y >= (175 / 3)) && (y <= 70)) && (yp >= (1.2 * y)))) {  // 1.2 = 6/5
                        return D; // Zone D
                    } else {
                        return B; // Zone B
                    }                         // End of 4th if
                }                             // End of 3rd if
            }                                 // End of 2nd if
        }                                     // End of 1st if
    }
    
    /**
     * Clarke error grid of the estimated engine.data.
     *
     * @param y Reference glucose values
     * @param yp Estimated glucose values
     *
     * @return For each region (A,B,C,D,E, key of the map), returns the number
     * of points that belong to it. The first position of the array corresponds
     * to region A, second to B and so on.
     */
    public static int[] calculateCEG(double[] y, double[] yp) {
        int[] total = new int[CLARKE_REGIONS];

        for (int i = 0; i < y.length; i++) {
            int region = determineCEGRegion(y[i],yp[i]);
            total[region] += 1;
        }

        return total;
    }

    public static String calculateCEGasString(double[] y, double[] yp) {
        int[] ceg = calculateCEG(y, yp);
        String cegStr = "A: " + ceg[A];
        cegStr += ", B: " + ceg[B];
        cegStr += ", C: " + ceg[C];
        cegStr += ", D: " + ceg[D];
        cegStr += ", E: " + ceg[E];
        return cegStr;
    }

    /**
     * Clarke error grid of the estimated engine.data, computed as a double value.
     *
     * @param y Reference glucose values
     * @param yp Estimated glucose values
     *
     * @return For each region (A,B,C,D,E, key of the map), obtains the number
     * of points that belong to it and then calculates a weighted sum of these
     * values.
     */    
    public static double computeCEG(double[] y, double[] yp) {
        int[] ceg = calculateCEG(y, yp);
        
        // Logaritmic scale: 
        double fitness = (ceg[B] * penaltyB) + 
                         (ceg[C] * penaltyC) +
                         (ceg[D] * penaltyD) + 
                         (ceg[E] * penaltyE);
        
        return fitness;
    }

    
    /**
     * Absolute error wighted with CEG zone.
     * 
     * @param y Reference glucose values
     * @param yp Estimated glucose values
     *
     * @return For 
     */
    public static double computeAbsoluteErrorWeightedCED(double[] y, double[] yp) {
        double error = 0.0;
        int region;
        double weight = 1.0;
        for (int k = 0; k < y.length; ++k) {
            region = determineCEGRegion(y[k],yp[k]);
//            switch (region) {
//                case B:
//                    weight = 10.0;
//                    break;
//                case C:
//                    weight = 100.0;
//                    break;
//                case D:
//                    weight = 1000.0;
//                    break;
//                case E:
//                    weight = 10000.0;
//                    break;
//                default: // Region A
//                    weight = 1.0;
//            }
            error += ((region+1) * 2 * Math.abs(y[k] - yp[k]));
        }
        return error;
    }

    public static double computeRSquare(double[] expected, double[] observed) {

        // Implementing this method: https://support.office.com/es-es/article/COEFICIENTE-R2-funci%C3%B3n-COEFICIENTE-R2-d7161715-250d-4a01-b80d-a8364f2be08f
        double avgE = StatUtils.mean(expected);
        double avgO = StatUtils.mean(observed);

        double acuNum = 0.0;
        double acuE = 0.0;
        double acuO = 0.0;
        
        for (int i=0; i<expected.length; i++) {        
            acuNum += ((expected[i]-avgE) * (observed[i]-avgO));
            acuE += Math.pow((expected[i]-avgE), 2);
            acuO += Math.pow((observed[i]-avgO), 2);
        }
               
        double r = acuNum / Math.sqrt(acuE * acuO);
        
        return Math.pow(r, 2);
    }

    /**
     * Absolute error between observed and expected values.
     * 
     * @param expected
     * @param observed
     * @return 
     */
    public static double computeAbsoluteError(double[] expected, double[] observed) {
        double error = 0.0;
        for (int k = 0; k < expected.length; ++k) {
            error += Math.abs(expected[k] - observed[k]);
        }
        return error;        
    }

    /**
     * Sets the values of the penalty coeficients for the CEG objective function.
     * @param props 
     */
    public static void setCEGPenalties(Properties props) {
        if (props.getProperty(com.engine.data.Common.CEG_PENALTY_B) != null) {
            penaltyB = Double.valueOf(props.getProperty(com.engine.data.Common.CEG_PENALTY_B));
        } else {
            penaltyB = DEFAULT_CEG_PENALTY_B;
        }
        
        if (props.getProperty(com.engine.data.Common.CEG_PENALTY_C) != null) {
            penaltyC = Double.valueOf(props.getProperty(com.engine.data.Common.CEG_PENALTY_C));
        } else {
            penaltyC = DEFAULT_CEG_PENALTY_C;
        }
        
        if (props.getProperty(com.engine.data.Common.CEG_PENALTY_D) != null) {
            penaltyD = Double.valueOf(props.getProperty(com.engine.data.Common.CEG_PENALTY_D));
        } else {
            penaltyD = DEFAULT_CEG_PENALTY_D;
        }
        
        if (props.getProperty(com.engine.data.Common.CEG_PENALTY_E) != null) {
            penaltyE = Double.valueOf(props.getProperty(com.engine.data.Common.CEG_PENALTY_E));
        } else {
            penaltyE = DEFAULT_CEG_PENALTY_E;
        }
    }

}
