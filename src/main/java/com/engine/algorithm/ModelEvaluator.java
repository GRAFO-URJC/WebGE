package com.engine.algorithm;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class ModelEvaluator {

    public static int objective;

    private ModelEvaluator() {
        //Private constructor to hide the public one
    }

    private static SimpleRegression createRegressionMatrix(double[]expected, double[] observed) {
        SimpleRegression sr = new SimpleRegression();
        for (int i = 0; i < expected.length; i++) {
            sr.addData(expected[i], observed[i]);
        }
        return sr;
    }


    public static double computeR2(double[]expected, double[] observed) {
        return createRegressionMatrix(expected, observed).getRSquare();
    }


    public static double computeAbsoluteError(double[] expected, double[] observed) {
        double error = 0.0;
        for (int k = 0; k < expected.length; ++k) {
            error += Math.abs(expected[k] - observed[k]);
        }
        return error;
    }

    private static double computeRelativeError(double[] expected, double[] prediction) {
        // Mean squared error:
        double acu = 0;

        for (int i = 0; i < expected.length; i++) {
            acu += Math.abs(expected[i]- prediction[i]) * 100.0 / expected[i];
        }

        return acu / (double) prediction.length;
    }


    public static double computeMSE(double[] expected, double[] observed) {
        // Mean squared error:
        double acu = 0;

        for (int i = 0; i < expected.length; i++) {
            acu += Math.pow(expected[i]- observed[i], 2);
        }

        return acu / (double) observed.length;
    }


    /**
     * Computes the Root Mean Squared Error (RMSE) between two arrays of data.
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


    public static double calculateObjective(String[][] target, String[] prediction, String objective) {
        double[] targetDouble;
        double[] predictionDouble;
        String[] oneDTarget = convert2dTo1d(target);
        String[] auxTarget = Arrays.copyOfRange(oneDTarget, 1, oneDTarget.length);
        String[] auxPrediction = Arrays.copyOfRange(prediction, 1, prediction.length);

        targetDouble = Arrays.stream(auxTarget)
                .mapToDouble(Double::parseDouble)
                .toArray();
        predictionDouble = Arrays.stream(auxPrediction)
                .mapToDouble(Double::parseDouble)
                .toArray();

        switch (objective) {
            case "R2":
                return 1.0 - computeR2(targetDouble, predictionDouble);
            case "ABS":
                return computeAbsoluteError(targetDouble, predictionDouble);
            case "MSE":
                return computeMSE(targetDouble, predictionDouble);
            case "REL":
                return computeRelativeError(targetDouble, predictionDouble);
            default:
                // RMSE
                return Math.sqrt(computeMSE(targetDouble, predictionDouble));
        }

    }

    //Method to get the first colum from an array
    private static String[] convert2dTo1d(String[][] array) {
        String[] oneDimensional = new String[array.length];
        for (int i = 0; i < array.length; i++)
            oneDimensional[i] = array[i][0];
        return oneDimensional;
    }



}
