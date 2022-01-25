package com.engine.algorithm;

import jeco.core.problem.Solution;
import jeco.core.problem.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Stores the elements that describe a solution of the problem where GE and DE are combined.
 */
public class SolutionDEGE {

    private double cost;
    private double relativeError;
    private String model;
    private Map<String,Double> parameterValues;

    private double testCost;
    private double testRelativeError;
    private double[] trainingPrediction;
    private double[] testPrediction;

    private static final String relativeErrorString = "\t"+"Relative error=";
    private static final String tCostString= "\t"+"Cost=";

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setParameterValues(Map<String,Double> parameterValues) {
        this.parameterValues = parameterValues;
    }

    public String getModel() {
        return model;
    }

    public Map<String,Double> getParameterValues() {
        return parameterValues;
    }

    public void setRelativeError(double relativeError) {
        this.relativeError = relativeError;
    }

    @Override
    public String toString() {
        StringBuilder trainPredStr = new StringBuilder();
        for (int i=0; i<trainingPrediction.length; i++) {
            trainPredStr.append(trainingPrediction[i]).append(";");
        }
        StringBuilder testPredStr = new StringBuilder();
        for (int i=0; i<testPrediction.length; i++) {
            testPredStr.append(testPrediction[i]).append(";");
        }

        return "\nModel="+ model + "\n" + "Parameter values=" + parameterValues + "\n" +
                "Training:\n" + tCostString + cost + "\n" + relativeErrorString + relativeError + "\n" +
                "\tTraining prediction: ;"+trainPredStr + "\n" +
                "Test:\n" + tCostString + testCost + "\n" + relativeErrorString + testRelativeError + "\n" +
                "\tTest prediction: ;"+testPredStr + "\n";
    }

    public String evaluationReport() {
        StringBuilder predStr = new StringBuilder();
        for (int i=0; i<trainingPrediction.length; i++) {
            predStr.append(trainingPrediction[i]).append(";");
        }

        return "\nModel="+ model + "\n" + "Parameter values=" + parameterValues + "\n" +
                "Evaluation:\n" + tCostString + cost + "\n" + relativeErrorString + relativeError + "\n" +
                "\tPrediction: ;"+predStr + "\n";
    }

    public void setTestRelativeError(double testRelativeError) {
        this.testRelativeError = testRelativeError;
    }

    public void setTestCost(double testCost) {
        this.testCost = testCost;
    }

    public Map<String,Double> obtainParameterValues(Solution<Variable<?>> sol, List<String> parameters) {
        Map<String,Double> parameterValuesNew = new HashMap<>(sol.getVariables().size());
        // Include the values of the parameters:
        for (int j = 0; j < sol.getVariables().size(); j++) {
            parameterValuesNew.put(parameters.get(j), (Double) sol.getVariable(j).getValue());
        }

        return parameterValuesNew;
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
