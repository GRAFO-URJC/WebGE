package com.gramevapp.web.model;

public class RunResultsDto {
    private int[] runIndex;
    private String[] model;
    private double[] trainingAVG;
    private double[] trainingRMSE;
    private double[] trainingAbs;
    private double[] trainingR2;
    private double[] testAVG;
    private double[] testRMSE;
    private double[] testAbs;
    private double[] testR2;

    public RunResultsDto(int size, boolean haveTest) {
        runIndex = new int[size];
        model = new String[size];
        trainingAVG = new double[size];
        trainingRMSE = new double[size];
        trainingAbs = new double[size];
        trainingR2 = new double[size];
        if (haveTest) {
            testAVG = new double[size];
            testRMSE = new double[size];
            testAbs = new double[size];
            testR2 = new double[size];
        }
    }

    public int[] getRunIndex() {
        return runIndex;
    }

    public void setRunIndex(int[] runIndex) {
        this.runIndex = runIndex;
    }

    public String[] getModel() {
        return model;
    }

    public void setModel(String[] model) {
        this.model = model;
    }

    public double[] getTrainingAVG() {
        return trainingAVG;
    }

    public void setTrainingAVG(double[] trainingAVG) {
        this.trainingAVG = trainingAVG;
    }

    public double[] getTrainingRMSE() {
        return trainingRMSE;
    }

    public void setTrainingRMSE(double[] trainingRMSE) {
        this.trainingRMSE = trainingRMSE;
    }

    public double[] getTrainingAbs() {
        return trainingAbs;
    }

    public void setTrainingAbs(double[] trainingAbs) {
        this.trainingAbs = trainingAbs;
    }

    public double[] getTrainingR2() {
        return trainingR2;
    }

    public void setTrainingR2(double[] trainingR2) {
        this.trainingR2 = trainingR2;
    }

    public double[] getTestAVG() {
        return testAVG;
    }

    public void setTestAVG(double[] testAVG) {
        this.testAVG = testAVG;
    }

    public double[] getTestRMSE() {
        return testRMSE;
    }

    public void setTestRMSE(double[] testRMSE) {
        this.testRMSE = testRMSE;
    }

    public double[] getTestAbs() {
        return testAbs;
    }

    public void setTestAbs(double[] testAbs) {
        this.testAbs = testAbs;
    }

    public double[] getTestR2() {
        return testR2;
    }

    public void setTestR2(double[] testR2) {
        this.testR2 = testR2;
    }
}
