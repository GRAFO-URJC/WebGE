package com.engine.algorithm;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class ModelEvaluator {

    private static double[][] trainingData;
    private static double[][] testData;
    private static Logger logger;

    public static final int ROOT_MEAN_SQUARED_ERROR = 0;
    public static final int MEAN_SQUARED_ERROR = 1;
    public static final int R2 = 2;
    public static final int ABSOLUTE_ERROR = 3;
    public static final int RELATIVE_ERROR = 4;
    public static int objective;


    private ModelEvaluator() {
        //Private constructor to hide the public one
    }

    public static double[][] loadData(String dataPath) throws IOException {

        // Firstly, a list is created to account for the number of elements to instantiate in the matrix.
        ArrayList<String> lines = new ArrayList<>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(dataPath)));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                lines.add(line);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            logger.info("Training file not found: " + e.getLocalizedMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }


        // Process the list
        double[][] matrix = new double[lines.size()][];

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split(";");
            matrix[i] = new double[parts.length];
            for (int j = 0; j < parts.length; ++j) {
                matrix[i][j] = Double.valueOf(parts[j]);
            }
        }

        return matrix;
    }


    public static void loadTrainingData(String dataPath) throws IOException {
        trainingData = loadData(dataPath);
    }

    public static void loadTestData(String dataPath) throws IOException {
        testData = loadData(dataPath);
    }


    /**
     * Generates a prediction with the model using the values from the input variables
     * in the trainin data, and then calls to the objective function selected.
     *
     * @param model
     * @return
     */
    public static double evaluateModel(String model) {
        // Use exp4j: http://projects.congrace.de/exp4j/

        ExpressionBuilder eb = includeInputVariables(model);
        Expression exp = eb.build();

        return evaluateModel(exp);

    }


    /**
     * Includes the input variables in the expression builder, but not the values.
     *
     * @param model
     * @return
     */
    public static ExpressionBuilder includeInputVariables(String model) {

        ExpressionBuilder eb = new ExpressionBuilder(model);
        // Include the input variables starting from the last one:
        for (int j = trainingData[0].length - 1; j > 0; j--) {
            eb.variable("X" + j);
        }

        return eb;
    }

    /**
     * Evaluates the model given by one expression with no input values.
     *
     * @param exp
     * @return
     */
    public static double evaluateModel(Expression exp) {

        // Generation of the prediction:

        // Array for predicted values
        double[] prediction = obtainPrediction(exp, trainingData);

        //return calculateObjective(trainingData, prediction);
        return 0.0;
    }


    /**
     * Generates a prediction in relation to the given data.
     *
     * @param data
     * @return
     */
    private static double[] obtainPrediction(Expression exp, double[][] data) {
        // Array for predicted values
        double[] prediction = new double[data.length];

        for (int t = 0; t < data.length; t++) {

            double res;

            // Include the values of the input variables starting from the last one:
            // Use exp4j: http://projects.congrace.de/exp4j/
            for (int j = data[0].length - 1; j > 0; j--) {
                exp.setVariable("X" + j, data[t][j]);
            }

            res = exp.evaluate();

            prediction[t] = res;

        }

        return prediction;
    }


    /**
     * Returns the result of calculating the objective function.
     *
     * @param expected
     * @param prediction
     */
    public static double calculateObjective(double[] expected, double[] prediction) {

        switch (objective) {
            case R2:
                return 1.0 - computeR2(expected, prediction);
            case ABSOLUTE_ERROR:
                return computeAbsoluteError(expected, prediction);
            case MEAN_SQUARED_ERROR:
                return computeMSE(expected, prediction);
            case RELATIVE_ERROR:
                return computeRelativeError(expected, prediction);
            default:
                // RMSE
                return Math.sqrt(computeMSE(expected, prediction));
        }
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


    /**
     * Calculates the relative error given the corresponding matrix.
     *
     * @param expected
     * @param prediction
     * @return
     */
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
    /* *
     * Evaluates a final solution, and includes more stats.
     * @param sol
     * /
    public static void evaluateSolution(SolutionEnergyModel sol) {

        // Create expression with the identifiers of the input variables
        ExpressionBuilder eb = includeInputVariables(sol.getModel());

        // Add the parameters:
        for (String p : sol.getParameterValues().keySet()) {
            eb.variable(p);
        }

        // Build expression and include parameter values:
        Expression exp = eb.build();
        exp.setVariables(sol.getParameterValues());

        // Obtain prediction:
        double[] prediction = obtainPrediction(exp,trainingData);
        sol.setTrainingPrediction(prediction);

        // Cost:
        sol.setCost(calculateObjective(trainingData,prediction));

        // Relative error:
        sol.setRelativeError(calculateRelativeError(trainingData,prediction));

        // Test prediction:
        if (testData != null) {
            double[] testPrediction = obtainPrediction(exp,testData);
            sol.setTestPrediction(testPrediction);

            // Cost:
            sol.setTestCost(calculateObjective(testData,testPrediction));

            // Relative error:
            sol.setTestRelativeError(calculateRelativeError(testData,testPrediction));
        }

    }
    */


/*
    public static void main(String[] args) {
        double[][] e = new double[5][1];
        double[] p = new double[5];

        e[0][0] = 1.0;
        e[1][0] = 2.0;
        e[2][0] = 3.0;
        e[3][0] = 4.0;
        e[4][0] = 5.0;

        p[0] = 1.0;
        p[1] = 2.1;
        p[2] = 3.0;
        p[3] = 4.1;
        p[4] = 5.0;

        System.out.println("R2: "+computeR2(e,p));

        System.out.println("MSE: "+computeMSE(e,p));

    }
*/


}
