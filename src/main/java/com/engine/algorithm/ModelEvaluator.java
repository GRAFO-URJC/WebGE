package com.engine.algorithm;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.*;

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

        return acu / prediction.length;
    }


    public static double computeMSE(double[] expected, double[] observed) {
        // Mean squared error:
        double acu = 0;

        for (int i = 0; i < expected.length; i++) {
            acu += Math.pow(expected[i]- observed[i], 2);
        }

        return acu / observed.length;
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


    /**
     * Return the Weighted Accuracy of a prediction as 1- accuracy*0.5 - F1*0.5
     * @param expected
     * @param observed
     * @return
     */
    public static double computeWeightedAccuracy(double[] expected, double[] observed) {
        double accu = 0.0;

        double total = expected.length;
        double truePositives = 0;

        if(expected.length != observed.length) {
            throw new RuntimeException("Not same amount of results");
        }

        for(int i = 0;i < expected.length; i++) {
            if(expected[i] == observed[i]) {
                truePositives++;
            }
        }

        double f1 = computeF1(expected, observed);

        accu = 1 - (truePositives/total)*0.5 - f1*0.5;

        return accu;
    }

    /**
     * Computes the F1 score for both multiclass and binary prediction, for multiclass it makes the
     * @param expected
     * @param observed
     * @return F1-value
     */
    private static double computeF1(double[] expected, double[] observed) {
        Map<Double, Integer> numberClasses = new HashMap<>();
        Map<Double, Integer> numberClasses2 = new HashMap<>();
        List<Double> indexes = new ArrayList<Double>();

        //Counts the amount of actual classes and observed classes of a prediction
        for(int i = 0; i < expected.length; i++) {
            if(numberClasses.containsKey(expected[i])) {
                numberClasses.put(expected[i], numberClasses.get(expected[i])+1);
            }else{
                numberClasses.put(expected[i], 1);
                indexes.add(expected[i]);
            }

            if(numberClasses2.containsKey(observed[i])) {
                numberClasses2.put(observed[i], numberClasses2.get(observed[i])+1);
            }else{
                numberClasses2.put(observed[i], 1);
            }
        }


        double macroAverageF1 = 0;
        if(numberClasses.size() > 2) { //if we have more than 2 classes we compute the F1 value for each class
            double recalls[] = new double[numberClasses.size()];
            int truePositives[] = new int[numberClasses.size()];

            for(int i = 0; i < expected.length; i++) { //True positives of each class
                if(expected[i] == observed[i]) {
                    truePositives[indexes.indexOf(expected[i])]++;

                }
            }


            for(int i = 0; i <indexes.size(); i++) { // Recall values of each class
                recalls[i] = (truePositives[i]*1.0)/numberClasses.get(indexes.get(i));
            }

            double precision[] = new double[numberClasses.size()];

            for(int i = 0; i <indexes.size(); i++) {
                if(numberClasses2.containsKey(indexes.get(i))) { //Precision values of each class
                    precision[i] = (truePositives[i]*1.0)/numberClasses2.get(indexes.get(i));
                }else {
                    precision[i] = 0;
                }
            }

            for(int i = 0; i < recalls.length; i++) {
                //If the class exists in the prediction we calculate its F1 values, if not we consider it 0
                if(recalls[i] != 0 || precision[i] != 0) {
                    macroAverageF1 += 2*((recalls[i]*precision[i])/(recalls[i]+precision[i]));
                }
            }

            macroAverageF1 = macroAverageF1/recalls.length; //Compute the macroAverageF1

        }else {

            double recalls = 0;
            int truePositives = 0;

            for(int i = 0; i < expected.length; i++) {
                if((expected[i] == 0) && (expected[i] == observed[i])) {
                    truePositives++;

                }
            }

            recalls = (truePositives*1.0)/numberClasses.get(0.0);

            double precision = 0;
            if(numberClasses2.containsKey(0.0)) {
                precision = (truePositives*1.0)/numberClasses2.get(0.0);
            }else {
                precision = 0;
            }

            //If the class exists in the prediction we calculate its F1 values, if not we consider it 0
            if (recalls == 0 && precision == 0) {
                macroAverageF1 = 0;
            }else {
                macroAverageF1 = 2*((recalls*precision)/(recalls+precision));
            }


        }

        return macroAverageF1;
    }

    /**
     * Compute the Harmonic mean of the recall of each class (both for multiclass and binary prediction)
     * @param expected
     * @param observed
     * @return 1-HarmonicMeanRecalls
     */
    public static double computeRecall(double[] expected, double[] observed) {
        //Number of classes to compute
        Map<Double, Integer> numberClasses = new HashMap<>();
        List<Double> indexes = new ArrayList<Double>();


        List<Integer> truePositives = new ArrayList<Integer>();

        for(int i = 0; i < expected.length; i++) {
            //Count how many classes we have
            if(numberClasses.containsKey(expected[i])) {
                numberClasses.put(expected[i], numberClasses.get(expected[i])+1);

            }else{
                numberClasses.put(expected[i], 1);
                indexes.add(expected[i]);
                truePositives.add(indexes.indexOf(expected[i]), 0); //Initialize the true positives to 0

            }

            if(expected[i] == observed[i]) {

                truePositives.set(indexes.indexOf(expected[i]), truePositives.get(indexes.indexOf(expected[i]))+1);
            }


        }

        double recalls[] = new double[numberClasses.size()];
        double macroAverageRecall = 0;

        for(int i = 0; i <indexes.size(); i++) {
            recalls[i] = (truePositives.get(i)*1.0)/numberClasses.get(indexes.get(i));
        }


        macroAverageRecall = macroAverageRecall/recalls.length;

        double harmonicMean = recalls.length;
        double inverses= 0;
        for(int i = 0; i < recalls.length; i++) {
            inverses += 1.0/recalls[i];
        }
        harmonicMean = harmonicMean/inverses;


        return 1-harmonicMean;
    }


    /**
     * Compute the Harmonic mean of the precision of each class
     * @param expected
     * @param observed
     * @return 1- HarmonicMeanPrecision
     */
    public static double computePrecision(double[] expected, double[] observed) {
        Map<Double, Integer> numberClasses = new HashMap<>();
        Map<Double, Integer> realClases = new HashMap<>();
        List<Double> indexes = new ArrayList<Double>();
        for(int i = 0; i < observed.length; i++) {
            if(numberClasses.containsKey(observed[i])) {
                numberClasses.put(observed[i], numberClasses.get(observed[i])+1);
            }else{
                numberClasses.put(observed[i], 1);
                indexes.add(observed[i]);
            }

            if(!realClases.containsKey(expected[i])) {
                realClases.put(expected[i], 1);
            }
        }

        double precision[] = new double[realClases.size()];
        int truePositives[] = new int[realClases.size()];

        for(int i = 0; i < expected.length; i++) {
            if(expected[i] == observed[i]) {
                truePositives[indexes.indexOf(observed[i])]++;

            }
        }


        for(int i = 0; i <indexes.size(); i++) {
            if(numberClasses.containsKey(indexes.get(i))) {
                precision[i] = (truePositives[i]*1.0)/numberClasses.get(indexes.get(i));
            }else {
                precision[i] = 0;
            }
        }

        //Macro average precision
        double macroAveragePrecision = 0;

        for(int i = 0; i < precision.length; i++) {
            macroAveragePrecision += precision[i];
        }

        macroAveragePrecision = macroAveragePrecision/precision.length; //Macro average

        //return 1-macroAveragePrecision;

        //Harmonic mean precision
        double harmonicMean = precision.length;
        double inverses= 0;
        for(int i = 0; i < precision.length; i++) {
            inverses += 1.0/precision[i];
        }
        harmonicMean = harmonicMean/inverses;

        return 1-harmonicMean;

    }

    /**
     * Computes the ConfusionMatrix of a prediction, as long as the classes go from 0 to n-1 for a nxn matrix
     *
     * @param expected
     * @param observed
     * @param percentaje (Whether the result will be returned as a percentage or as the number of occurrences)
     * @return
     */
    public static double[][] computeConfusionMatrix(double[] expected, double[] observed, boolean percentaje){


        //Number of classes in the real data
        Map<Double, Integer> realNumberClasses = new HashMap<Double, Integer>();
        for(double d: expected) {

            if(realNumberClasses.containsKey(d)) {
                realNumberClasses.put(d, realNumberClasses.get(d)+1);

            }else {
                realNumberClasses.put(d, 1);
            }

        }

        //Confusion matrix
        double[][] confusionMatrix = new double[realNumberClasses.size()][realNumberClasses.size()];

        //For all the observed data we are going to add it to the matrix
        for(int i = 0; i < observed.length; i++) {

            confusionMatrix[(int) expected[i]][(int) observed[i]]++;

        }

        if(percentaje) { //if percentaje is true we return the percentaje of true and false predictions for each class with 4 decimals accuracy

            for(int i = 0; i < confusionMatrix.length; i++) {
                for(int j = 0; j < confusionMatrix[i].length; j++) {

                    confusionMatrix[i][j] = confusionMatrix[i][j]/realNumberClasses.get(i *1.0);
                    confusionMatrix[i][j] = Math.round(confusionMatrix[i][j] * 10000) / 10000.0;

                }
            }


        }

        return confusionMatrix;
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
            case "WA":
                return computeWeightedAccuracy(targetDouble, predictionDouble);
            case"RECALL":
                return computeRecall(targetDouble, predictionDouble);
            case "PRECISION":
                return computePrecision(targetDouble, predictionDouble);
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
