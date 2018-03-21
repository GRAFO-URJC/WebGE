package engine.algorithm;

import java.util.ArrayList;
import org.apache.commons.math3.distribution.BetaDistribution;

/**
 * Class to evaluate a population.
 *
 * @author J. Manuel Colmenar, Jose Luis Risco Martin
 */
public abstract class AbstractPopEvaluator {

    protected ArrayList<double[]> timeTable;
    protected BetaDistribution bd;
    protected static int realDataCopied = 1;
    protected int modelWidth = 0;

    public void setRealDataCopied(int realDataCopied) {
        this.realDataCopied = realDataCopied;
    }

    public int getRealDataCopied() {
        return realDataCopied;
    }
   
    public abstract double evaluate(int idxExpr, int k);
    
    public void evaluateExpression(int idxExpr) {
        double[] rowPred;
        
        int k = 0;
        while ((k < realDataCopied) && (k < timeTable.size())) {
            rowPred = timeTable.get(k);
            rowPred[rowPred.length - 1] = rowPred[0];
            k++;
        }
        
        if (modelWidth == 0) {
            // Evaluate all the engine.data
            for (int k2 = realDataCopied; k2 < timeTable.size(); k2++) {
                rowPred = timeTable.get(k2);
                rowPred[rowPred.length - 1] = evaluate(idxExpr, k2);
            }
        } else {
            // Evaluate model width
            int limit = k + modelWidth;
            while ((k < limit) && (k < timeTable.size())) {
                rowPred = timeTable.get(k);
                rowPred[rowPred.length - 1] = evaluate(idxExpr, k);
                k++;
            }
            // Copy the rest
            for (int k2 = k; k2 < timeTable.size(); k2++) {
                rowPred = timeTable.get(k2);
                rowPred[rowPred.length - 1] = rowPred[0];
            }
        }
    }

    public void setTimeTable(ArrayList<double[]> timeTable) {
        this.timeTable = timeTable;
    }

    public ArrayList<double[]> getTimeTable() {
        return timeTable;
    }

    public double getVariable(int idxVar, int k) {
        if (k < 0) {
            return timeTable.get(0)[idxVar];
        } else {
            return timeTable.get(k)[idxVar];
        }
    }

    public double getPrevData(int numData, int currK, int idxVar) {
        int num = 0;
        int k = currK;
        double val = 0.0;
        while ((num < numData) && (k > 0)) {
            k--;
            double currVal = timeTable.get(k)[idxVar];
            if (currVal > 0) {
                num++;
                val = currVal;
            }
        }
        return val;
    }
    
    /**
     * If no engine.data is found, returns 0.
     * 
     * @param numData
     * @param currK
     * @param idxVar
     * @return 
     */
    public int getPrevDataDistance(int numData, int currK, int idxVar) {
        int num = 0;
        int k = currK;
        while ((num < numData) && (k > 0)) {
            k--;
            double currVal = timeTable.get(k)[idxVar];
            if (currVal > 0) {
                num++;
            }
        }
        
        if ((k == 0) && (num < numData)) {
            // No previous engine.data was found
            return 0;
        } else {
            return currK - k;
        }
    }

    /**
     * Returns the maximum value of the variable idxVar considering the number
     * of past engine.data as indicated by the window.
     * 
     * @param window
     * @param currK
     * @param idxVar
     * @return 
     */
    public double getMaxValue(int window, int currK, int idxVar) {
        double max = 0;
        
        int k = currK;
        while (((currK-k) < window) && (k > 0)) {
            k--;
            if (timeTable.get(k)[idxVar] > max) {
                max = timeTable.get(k)[idxVar];
            }
        }
        
        return max;
    }
    
    public int getMaxValueDistance(int window, int currK, int idxVar) {
        double max = 0;
        int dist = 0; 
        
        int k = currK;
        while (((currK-k) < window) && (k > 0)) {
            k--;
            if (timeTable.get(k)[idxVar] > max) {
                max = timeTable.get(k)[idxVar];
                dist = currK - k;
            }
        }
        
        return dist;
    }
    
    /**
     * Returns the sum of the values of idxVar within the past engine.data within the
     * window.
     * 
     * @param window
     * @param currK
     * @param idxVar
     * @return 
     */
    public double getSumOfValues(int window, int currK, int idxVar) {
        double sum = 0;
        
        int k = currK;
        while (((currK-k) < window) && (k > 0)) {
            k--;
            sum += timeTable.get(k)[idxVar];
        }
        
        return sum;
    }
    
    public double realData(int k) {
        // Real engine.data is the first column
        return getVariable(0, k);
    }

    public double predictedData(int k) {
        // Predicted engine.data is the last column
        return getVariable(timeTable.get(0).length - 1, k);
    }

    public double beta(double x, int a, int b) {
          bd = new BetaDistribution(a, b);
          return bd.density(x);
    }

    public int getModelWidth() {
        return modelWidth;
    }

    public void setModelWidth(Integer modelWidth) {
        this.modelWidth = modelWidth;
    }
}
