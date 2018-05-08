package com.engine.data;

import com.engine.algorithm.AbstractPopEvaluator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import jeco.core.algorithm.moge.AbstractProblemGE;
import jeco.core.problem.Solution;
import jeco.core.problem.Variable;
import com.engine.util.UtilStats;

public class NormalizedDataTable {

    private static final Logger logger = Logger.getLogger(NormalizedDataTable.class.getName());

    public static final int OBJ_RMSE = 0;
    public static final int OBJ_CEG = 1;
    public static final int OBJ_R2 = 3;
    public static final int OBJ_ABSERR = 4;
    
    public static int OBJECTIVE = OBJ_RMSE;
    
    protected AbstractProblemGE problem;
    protected String trainingPath = null;
    protected String validationPath = null;
    protected boolean viewResults = false;
    protected double errorThreshold = 0;
    protected ArrayList<double[]> trainingTable = new ArrayList<>();
    protected ArrayList<double[]> validationTable = new ArrayList<>();
    protected int numInputColumns = 0;
    protected int numTotalColumns = 0;
    protected double[] xLs = null;
    protected double[] xHs = null;

    protected double bestFitness;


    public NormalizedDataTable(AbstractProblemGE problem, String trainingPath, String validationPath,
            double errorThreshold, boolean viewResults, boolean normalizeData, int numData) {
        this.problem = problem;
        this.trainingPath = trainingPath;
        this.validationPath = validationPath;
        this.errorThreshold = errorThreshold;
        this.viewResults = viewResults;

        if (numData == 0) {
            numData = Integer.MAX_VALUE;
        }
        
        logger.info("Reading engine.data file ...");
        
        try {
            // Identify engine.data files:
            identifyAndFillDataTable(trainingPath,trainingTable,numData);
        } catch (IOException ex) {
            System.err.println("Error reading training file "+trainingPath+": " + ex.getLocalizedMessage());
        }
                
        if (validationPath != null) {
            try {
                identifyAndFillDataTable(validationPath,validationTable,numData);
            } catch (IOException ex) {
                System.err.println("Error reading validation file "+validationPath+": " + ex.getLocalizedMessage());
            }
        }
        
        logger.info("... done.");
        if (normalizeData) {
            normalize(1.0, 2.0);
        }    
        resetBest();
    }

    public final void fillDataTable(String dataPath, ArrayList<double[]> dataTable, int numData) throws IOException {
        if (numData == 0) {
            numData = Integer.MAX_VALUE;
        }
        BufferedReader reader = new BufferedReader(new FileReader(new File(dataPath)));
        String line;
        while ((numData > 0) && ((line = reader.readLine()) != null)) {
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split(";");
            if (parts.length > numInputColumns) {
                numInputColumns = parts.length;
                numTotalColumns = numInputColumns + 1;
            }
            // TODO: targetValue is not included.
            double[] dataLine = new double[numTotalColumns];
            for (int j = 0; j < numInputColumns; ++j) {
                dataLine[j] = Double.valueOf(parts[j]);
            }
            dataTable.add(dataLine);
            numData--;
        }
        reader.close();
    }

    public double evaluate(AbstractPopEvaluator evaluator, Solution<Variable<Integer>> solution, int solId) {
        String functionAsString = problem.generatePhenotype(solution).toString();
        String origFunctionAsString = problem.generatePhenotype(solution).toString();
        double fitness = computeFitness(evaluator, solId);
        if ((this.problem.getNumberOfObjectives() == 1) && (fitness < bestFitness)) {
            bestFitness = fitness;
            for (int i = 0; i < numTotalColumns; ++i) {
                if (i == 0) {
                    functionAsString = functionAsString.replaceAll("getVariable\\(" + i + ",", "yr\\(");
                } else if (i == numTotalColumns - 1) {
                    functionAsString = functionAsString.replaceAll("getVariable\\(" + i + ",", "yp\\(");
                } else {
                    functionAsString = functionAsString.replaceAll("getVariable\\(" + i + ",", "u" + i + "\\(");
                }
            }
            logger.info("Best FIT=" + bestFitness + "; Expression=" + functionAsString + "; Java Expression: "+ origFunctionAsString);

        }
        return fitness;
    }

    public double computeFitness(AbstractPopEvaluator evaluator, int solId) {
        try {
            evaluator.evaluateExpression(solId);
        } catch (ArithmeticException e) {
            // If any malfunction in the expression happens, max value is given
            return Double.MAX_VALUE;
        }
        ArrayList<double[]> timeTable = evaluator.getTimeTable();
        int modelWidth = evaluator.getModelWidth();
        int size;
        if (modelWidth == 0) {
            size = timeTable.size()-evaluator.getRealDataCopied();
        } else {
            size = modelWidth;
        }
        
        
        double[] realData = new double[size];
        double[] predictedData = new double[size];
        for (int k=0; k<size;k++) {
            realData[k] = timeTable.get(k+evaluator.getRealDataCopied())[0];
            predictedData[k] = timeTable.get(k+evaluator.getRealDataCopied())[timeTable.get(k).length - 1];
        }
        
        double fitness = Double.MAX_VALUE;
        
        switch (OBJECTIVE) {
            case OBJ_RMSE:
                fitness = UtilStats.computeRMSE(realData, predictedData);
                break;
            case OBJ_CEG:
                fitness = UtilStats.computeCEG(realData, predictedData);
                //fitness = UtilStats.computeAbsoluteErrorWeightedCED(realData, predictedData);
                break;
            case OBJ_R2:
                double r2 = UtilStats.computeRSquare(realData, predictedData);
                if (Double.isInfinite(r2)) {
                    fitness = r2;
                } else {
                    fitness = 1.0 - r2;
                }
                break;
            case OBJ_ABSERR:
                fitness = UtilStats.computeAbsoluteError(realData, predictedData);
                break;
        }
        
/*        
        double fitness = 0;
        for (int i = 0; i < timeTable.size(); ++i) {
            double min = Math.min(timeTable.get(i)[numInputColumns], timeTable.get(i)[0]);
            double max = Math.max(timeTable.get(i)[numInputColumns], timeTable.get(i)[0]);
            if (1 - (min / max) > errorThreshold) {
                fitness += max - min;
            }
        }
*/
        return fitness;
    }

    public final void normalize(double yL, double yH) {
        logger.info("Normalizing engine.data in [" + yL + ", " + yH + "] ...");
        xLs = new double[numInputColumns];
        xHs = new double[numInputColumns];
        for (int i = 0; i < numInputColumns; ++i) {
            xLs[i] = Double.POSITIVE_INFINITY;
            xHs[i] = Double.NEGATIVE_INFINITY;
        }
        // We compute first minimum and maximum values:
        ArrayList<double[]> fullTable = new ArrayList<>();
        fullTable.addAll(trainingTable);
        fullTable.addAll(validationTable);
        for (int i = 0; i < fullTable.size(); ++i) {
            double[] row = fullTable.get(i);
            for (int j = 0; j < numInputColumns; ++j) {
                if (xLs[j] > row[j]) {
                    xLs[j] = row[j];
                }
                if (xHs[j] < row[j]) {
                    xHs[j] = row[j];
                }
            }
        }

        // Now we compute "m" and "n", being y = m*x + n
        // y is the new engine.data
        // x is the old engine.data
        double[] m = new double[numInputColumns];
        double[] n = new double[numInputColumns];
        for (int j = 0; j < numInputColumns; ++j) {
            m[j] = (yH - yL) / (xHs[j] - xLs[j]);
            n[j] = yL - m[j] * xLs[j];
        }
        // Finally, we normalize ...
        for (int i = 0; i < fullTable.size(); ++i) {
            double[] row = fullTable.get(i);
            for (int j = 0; j < numInputColumns; ++j) {
                row[j] = m[j] * row[j] + n[j];
            }
        }

        // ... and report the values of both xLs and xHs ...
        StringBuilder xLsAsString = new StringBuilder();
        StringBuilder xHsAsString = new StringBuilder();
        for (int j = 0; j < numInputColumns; ++j) {
            if (j > 0) {
                xLsAsString.append(", ");
                xHsAsString.append(", ");
            } else {
                xLsAsString.append("xLs=[");
                xHsAsString.append("xHs=[");
            }
            xLsAsString.append(xLs[j]);
            xHsAsString.append(xHs[j]);
        }
        xLsAsString.append("]");
        xHsAsString.append("]");
        logger.info(xLsAsString.toString());
        logger.info(xHsAsString.toString());
        logger.info("... done.");
    }

    public double computeAvgError(ArrayList<double[]> timeTable) {
        double error = 0.0;
        for (int k = 0; k < timeTable.size(); ++k) {
            double[] row = timeTable.get(k);
            error += Math.abs(row[0] - row[row.length - 1]);
        }
        error /= timeTable.size();
        return error;
    }

    public ArrayList<double[]> getTrainingTable() {
        return trainingTable;
    }

    public ArrayList<double[]> getValidationTable() {
        return validationTable;
    }

    public void resetBest() {
        bestFitness = Double.POSITIVE_INFINITY;
    }

    private void fillDataTableWithDataElements(ArrayList<DataElement> inputData,
            ArrayList<double[]> dataTable) {
        // This is HARDCODED!!! because of the nature of the engine.data:
        numInputColumns = 5;
        numTotalColumns = numInputColumns + 1;
        
        for (DataElement d : inputData) {
            double[] dataLine = new double[numTotalColumns];
            // ALSO HARDCODED!!!
            dataLine[0] = d.getGl();
            dataLine[1] = d.getCh();
            dataLine[2] = d.getInsulinBolus();
            dataLine[3] = d.getIl();
            dataLine[4] = d.getTargetGlucose();
            dataTable.add(dataLine);            
        }
        
    }

    private void identifyAndFillDataTable(String filePath, ArrayList<double[]> table, int numData) throws IOException {
        switch (DataLoader.identifyFileFormat(filePath)) {
            case ConstantsDataMngmt.AUSTRIA_FORMAT:
                ArrayList<DataElement> data = DataLoader.loadPreProcessedData(filePath, numData);
                fillDataTableWithDataElements(data, table);
                break;
            case ConstantsDataMngmt.CUSTOM_FORMAT_2:
                ArrayList<DataElement> data2 = DataLoader.load2ndCustomFormatData(filePath, numData);
                fillDataTableWithDataElements(data2, table);
                break;
            default:
                fillDataTable(filePath, table, numData);
        }
    }
}
