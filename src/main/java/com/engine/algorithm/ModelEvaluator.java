package com.engine.algorithm;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.Arrays;

public class ModelEvaluator {


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

    /**
     * Absolute error between observed and expected values.
     */
    public static double computeAbsoluteError(double[] expected, double[] observed) {
        double error = 0.0;
        for (int k = 0; k < expected.length; ++k) {
            error += Math.abs(expected[k] - observed[k]);
        }
        return error;
    }

    public static double computeAvgError(double[] expected, double[] observed) {
        double error = 0.0;
        for (int k = 0; k < expected.length; ++k) {
            error += Math.abs(expected[k] - observed[k]);
        }
        error /= expected.length;
        return error;
    }

    public static double computeRelativeError(double[] expected, double[] prediction) {
        double acu = 0;

        for (int i = 0; i < expected.length; i++) {
            acu += Math.abs(expected[i]- prediction[i]) / Math.abs(expected[i]);
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
                return computeRMSE(targetDouble, predictionDouble);
        }

    }

    //Method to get the first colum from an array
    private static String[] convert2dTo1d(String[][] array) {
        String[] oneDimensional = new String[array.length];
        for (int i = 0; i < array.length; i++)
            oneDimensional[i] = array[i][0];
        return oneDimensional;
    }




    public static double evaluateModel(Expression exp, String objective, String[][]data) {
        // Generation of the prediction:

        // Array for predicted values
        String[] prediction = obtainPrediction(exp,data);

        return calculateObjective(data,prediction, objective);

    }



    /**
     * Generates a prediction in relation to the given data.
     *
     * @param data
     * @return
     */
    private static String[] obtainPrediction(Expression exp, String[][] data) {
        // Array for predicted values
        String[] prediction = new String[data.length];

        for (int t = 1; t < data.length; t++) {

            String res;

            // Include the values of the input variables starting from the last one:
            // Use exp4j: http://projects.congrace.de/exp4j/
            for (int j = data[0].length - 1; j > 0; j--) {
                exp.setVariable("X" + j, Double.valueOf(data[t][j]));
            }

            res = String.valueOf(exp.evaluate());

            prediction[t] = res;

        }

        return prediction;
    }


    /**
     * Includes the input variables in the expression builder, but not the values.
     *
     * This method has to be separated because it is used by other classes.
     *
     * @param model
     * @return
     */
    public static ExpressionBuilder includeInputVariables(String model, String[][] func) {

        ExpressionBuilder eb = new ExpressionBuilder(model);
        // Include the input variables starting from the last one:
        for (int j = func[0].length - 1; j > 0; j--) {
            eb.variable("X" + j);
        }

        return eb;
    }


}
