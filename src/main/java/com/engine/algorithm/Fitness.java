/*
 * To change this license header, choose License Headers in Project ExpProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.algorithm;

import java.util.Arrays;

/**
 *
 * @author Carlos García Moreno
 */
public class Fitness {
    
    private double[] target;
    private double[] prediction;
    
    //The constructor, conver the target and the prediction to the same dimesion and length to be compared
    public Fitness(String[][] target, String[] prediction){
        String[] oneDTarget = convert2dTo1d(target);
        String[] auxTarget = Arrays.copyOfRange(oneDTarget, 1, oneDTarget.length);
        String[] auxPrediction = Arrays.copyOfRange(prediction, 1, prediction.length);
        
        this.target = Arrays.stream(auxTarget)
                        .mapToDouble(Double::parseDouble)
                        .toArray();
        this.prediction = Arrays.stream(auxPrediction)
                        .mapToDouble(Double::parseDouble)
                        .toArray();        
    }
    
    //Method to get the first colum from an array
    private String[] convert2dTo1d(String[][] array){
        String[] oneDimensional = new String[array.length];
        for(int i=0; i < array.length; i++)
            oneDimensional[i] = array[i][0];
        return oneDimensional;
    }
    
    //Error absolute acumulated
    public double ABSAcumulated(){
        if (target.length != prediction.length) {
            throw new IllegalArgumentException("array lengths are not equal");
        }
        
        double totError = 0.0;
        for(int i = 0; i < this.target.length; i++)
            totError += Math.abs(this.target[i] - this.prediction[i]);
        
        return totError;
    }
    
    //Mean squared error
    public double meanSquaredError(){
        if (target.length != prediction.length) {
            throw new IllegalArgumentException("array lengths are not equal");
        }
        
        int n = target.length;
        double rss = 0.0;
        for (int i= 0; i < n; i++)
            rss += Math.sqrt(Math.abs(target[i] - prediction[i]));
        
        return rss/n;
    }
    
    //Average error
    public double averageError(){
        if (target.length != prediction.length) {
            throw new IllegalArgumentException("array lengths are not equal");
        }
        
        int n = target.length;
        double totError = 0.0;
        for(int i = 0; i < n; i++)
            totError += Math.abs(this.target[i] - this.prediction[i]);
        
        return totError/n;
    }
    
    public double r2(){
        if (target.length != prediction.length) {
            throw new IllegalArgumentException("array lengths are not equal");
        }
        
        int n = target.length;
        
        // first pass
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (int i = 0; i < n; i++) {
            sumx  += prediction[i];
            sumx2 += prediction[i] * prediction[i];
            sumy  += target[i];
        }
        double xbar = sumx / n;
        double ybar = sumy / n;
        
        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++){
            xxbar += (prediction[i] - xbar) * (prediction[i] - xbar);
            yybar += (target[i] - ybar) * (target[i] - ybar);
            xybar += (prediction[i] - xbar) * (target[i] - ybar);
        }        
        double slope = xybar / xxbar;
        double intercept = ybar - slope * xbar;
        
        // more statistical analysis
        double ssr = 0.0; // regression sum of squares
        for (int i = 0; i < n; i++){
            double fit = slope * prediction[i] + intercept;
            ssr += (fit - ybar) * (fit - ybar);
        }
        
        return ssr / yybar;
    }
    
    public double test(){
        double totError = 0.0;
        for(int i = 0; i < this.target.length; i++){
            totError += Math.pow(this.target[i] - this.prediction[i], 2);
        }
        return totError;
}
}
