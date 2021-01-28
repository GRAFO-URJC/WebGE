package com.engine.algorithm;

import jeco.core.problem.Solution;
import jeco.core.problem.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;

/**
 * Stores the elements that describe a solution of the problem where GE and DE are combined.
 */
public class SolutionDEGE {

    private double cost;
    private double relativeError;
    private String model;
    private HashMap<String,Double> parameterValues;

    private double testCost;
    private double testRelativeError;
    private double[] trainingPrediction;
    private double[] testPrediction;

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setParameterValues(HashMap<String,Double> parameterValues) {
        this.parameterValues = parameterValues;
    }

    public String getModel() {
        return model;
    }

    public HashMap<String,Double> getParameterValues() {
        return parameterValues;
    }

    public void setRelativeError(double relativeError) {
        this.relativeError = relativeError;
    }

    @Override
    public String toString() {
        String trainPredStr = "";
        for (int i=0; i<trainingPrediction.length; i++) {
            trainPredStr += trainingPrediction[i]+";";
        }
        String testPredStr = "";
        for (int i=0; i<testPrediction.length; i++) {
            testPredStr += testPrediction[i]+";";
        }

        return "\nModel="+ model + "\n" + "Parameter values=" + parameterValues + "\n" +
                "Training:\n" + "\tCost=" + cost + "\n" + "\tRelative error=" + relativeError + "\n" +
                "\tTraining prediction: ;"+trainPredStr + "\n" +
                "Test:\n" + "\tCost=" + testCost + "\n" + "\tRelative error=" + testRelativeError + "\n" +
                "\tTest prediction: ;"+testPredStr + "\n";
    }

    public String evaluationReport() {
        String predStr = "";
        for (int i=0; i<trainingPrediction.length; i++) {
            predStr += trainingPrediction[i]+";";
        }

        return "\nModel="+ model + "\n" + "Parameter values=" + parameterValues + "\n" +
                "Evaluation:\n" + "\tCost=" + cost + "\n" + "\tRelative error=" + relativeError + "\n" +
                "\tPrediction: ;"+predStr + "\n";
    }

    public void setTestRelativeError(double testRelativeError) {
        this.testRelativeError = testRelativeError;
    }

    public void setTestCost(double testCost) {
        this.testCost = testCost;
    }

    public HashMap<String,Double> obtainParameterValues(Solution<Variable<?>> sol, ArrayList<String> parameters) {
        HashMap<String,Double> parameterValues = new HashMap<>(sol.getVariables().size());
        // Include the values of the parameters:
        for (int j = 0; j < sol.getVariables().size(); j++) {
            parameterValues.put(parameters.get(j), (Double) sol.getVariable(j).getValue());
        }

        return parameterValues;
    }

    public void setTrainingPrediction(double[] input) {
        this.trainingPrediction = new double[input.length];
        IntStream.range(0, input.length).forEach(i -> this.trainingPrediction[i] = input[i]);
    }

    public void setTestPrediction(double[] input) {
        this.testPrediction = new double[input.length];
        IntStream.range(0, input.length).forEach(i -> this.testPrediction[i] = input[i]);
    }
}
