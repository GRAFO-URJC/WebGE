/*
 * To change this license header, choose License Headers in Project ExpProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.algorithm;

import com.engine.util.UtilStats;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.RunService;
import jeco.core.algorithm.Algorithm;
import jeco.core.algorithm.ga.SimpleGeneticAlgorithm;
import jeco.core.algorithm.moge.AbstractProblemGE;
import jeco.core.algorithm.moge.Phenotype;
import jeco.core.operator.comparator.SimpleDominance;
import jeco.core.operator.crossover.SinglePointCrossover;
import jeco.core.operator.mutation.IntegerFlipMutation;
import jeco.core.operator.selection.TournamentSelect;
import jeco.core.problem.Solution;
import jeco.core.problem.Solutions;
import jeco.core.problem.Variable;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static com.engine.util.Common.currentDateTimeAsFormattedString;

/**
 * @author Carlos García Moreno, J. M. Colmenar
 */
public class SymbolicRegressionGE extends AbstractProblemGE {

    private final Logger logger = Logger.getLogger(SymbolicRegressionGE.class.getName());
    protected Evaluator evaluator;
    protected String[][] func;
    private HashMap<String, Integer> vars = new HashMap<>();

    protected Properties properties;
    private Solutions<Variable<Integer>> solutions;
    private String logPopulationOutputFile = new String();

    // Binary masks for logging:
    public final int LOG_GENOTYPE_MASK = 1;
    public final int LOG_USED_GENES_MASK = 2;
    public final int LOG_FITNESS_MASK = 4;
    public final int LOG_PHENOTYPE_MASK = 8;
    public final int LOG_EVALUATION_MASK = 16;

    private Algorithm<Variable<Integer>> algorithm;
    private boolean stop;

    public static final String REPORT_HEADER = "Obj.;Model;Time";
    public ArrayList<String> executionReport = new ArrayList<>();


    public SymbolicRegressionGE(Properties properties, int numObjectives) {
        super(properties.getProperty(com.engine.util.Common.BNF_PATH_FILE_PROP), numObjectives,
                Integer.parseInt(properties.getProperty(com.engine.util.Common.CHROMOSOME_LENGTH_PROP)),
                Integer.parseInt(properties.getProperty(com.engine.util.Common.MAX_WRAPS_PROP)),
                AbstractProblemGE.CODON_UPPER_BOUND_DEFAULT);

        this.properties = properties;

        if (this.properties.getProperty(com.engine.util.Common.SENSIBLE_INITIALIZATION) != null) { // Not initializated in properties
            this.setSensibleInitialization(true, Double.parseDouble(this.properties.getProperty(com.engine.util.Common.SENSIBLE_INITIALIZATION)));
        }

        this.evaluator = new Evaluator();
    }

    public void stopExecution() {
        stop = true;
        algorithm.stopExection();
    }

    @Override
    public void evaluate(Solution<Variable<Integer>> solution, Phenotype phenotype) {
        String originalFunction = phenotype.toString();

        //Create array of prediction
        String[] prediction = new String[func.length];
        prediction[0] = originalFunction;

        //Evaluation from phenotype
        for (int i = 1; i < func.length; i++) {
            String currentFunction = calculateFunctionValued(originalFunction, i);
            double funcI;
            try {
                String aux = this.evaluator.evaluate(currentFunction);
                if (aux.equals("NaN")) {//TODO revisar valores menores que 0
                    funcI = Double.POSITIVE_INFINITY;
                } else {
                    funcI = Double.parseDouble(aux);
                }
            } catch (EvaluationException ex) {
                Logger.getLogger(SymbolicRegressionGE.class.getName()).log(Level.SEVERE, null, ex);
                funcI = Double.POSITIVE_INFINITY;
            }
            //Add to prediction array the evaluation calculated
            prediction[i] = String.valueOf(funcI);
            solution.getProperties().put(String.valueOf(i), funcI);
        }

        // Calculate fitness
        Fitness fitness = new Fitness(func, prediction);
        double fValue = fitness.r2();

        // Control valid value as fitness
        if (Double.isNaN(fValue)) {
            solution.getObjectives().set(0, Double.POSITIVE_INFINITY);
        } else {
            // R2 best is 1, but we are minimizing
            solution.getObjectives().set(0, 1.0 - fValue);
        }
    }

    //Method to replace the unknowns variables by values
    private String calculateFunctionValued(String originalFunction, int index) {
        String newFunction = originalFunction;

        for (Map.Entry<String, Integer> stringIntegerEntry : vars.entrySet()) {
            String key = stringIntegerEntry.getKey().toUpperCase();
            int keyPosition = Integer.parseInt(stringIntegerEntry.getValue().toString());
            newFunction = newFunction.replace(key, func[index][keyPosition]);
        }
        return newFunction;
    }

    //Method to replace the unknowns variables by values
    public static Double calculateFunctionValuedResultWithCSVData(String originalFunction, String[] content) throws EvaluationException {
        String newFunction = originalFunction;
        Evaluator evaluatorForFunction = new Evaluator();

        String replacePart;
        for (int i = (content.length - 1); i > 0; i--) {
            replacePart = "X" + i;
            newFunction = newFunction.replaceAll(replacePart, content[i]);
        }

        return Double.valueOf(evaluatorForFunction.evaluate(newFunction));
    }

    @Override
    public SymbolicRegressionGE clone() {
        return new SymbolicRegressionGE(properties, this.numberOfObjectives);
    }


    /**
     * Method to run the GE algorithm with the provided properties.
     */
    public void runGE(RunGeObserver obs, String experimentDatatypeInfo, Run run, RunService runService) {
        // Load target data
        // TODO: NO distinguir entre training, validation y test.
        func = processExperimentDataTypeInfo(experimentDatatypeInfo);
        vars = getVariables(func);
        logger.setLevel(Level.ALL);

        // Log population:
        int logPopulation = Integer.parseInt(properties.getProperty(com.engine.util.Common.LOG_POPULATION_PROP));
        if (logPopulation > 0) {
            int gen = 0;
            String fileName = "Log_Population_" + currentDateTimeAsFormattedString() + ".csv";
            logPopulationOutputFile = properties.getProperty(com.engine.util.Common.LOGGER_BASE_PATH_PROP) + File.separator + fileName;
            // Report the elements that are logged:
            StringBuilder buffer = new StringBuilder("Reported stats;;");
            if ((logPopulation & LOG_GENOTYPE_MASK) == LOG_GENOTYPE_MASK) {
                buffer.append("Genotype;");
            }
            if ((logPopulation & LOG_USED_GENES_MASK) == LOG_USED_GENES_MASK) {
                buffer.append("Used genes;");
            }
            if ((logPopulation & LOG_FITNESS_MASK) == LOG_FITNESS_MASK) {
                buffer.append("Fitness;");
            }
            if ((logPopulation & LOG_PHENOTYPE_MASK) == LOG_PHENOTYPE_MASK) {
                buffer.append("Phenotype;");
            }
            if ((logPopulation & LOG_EVALUATION_MASK) == LOG_EVALUATION_MASK) {
                buffer.append("Evaluation;");
            }
            buffer.append("\n");
            addToLogFile(buffer.toString());
        }

        int numObjectives = 1;
        if ((properties.getProperty(com.engine.util.Common.OBJECTIVES_PROP) != null)
                && (Integer.parseInt(properties.getProperty(com.engine.util.Common.OBJECTIVES_PROP)) == 2)) {
            numObjectives = 2;
        }

        // Adjust some properties
        double crossOverProb = SinglePointCrossover.DEFAULT_PROBABILITY;
        if (properties.getProperty(com.engine.util.Common.CROSSOVER_PROB_PROP) != null) {
            crossOverProb = Double.parseDouble(properties.getProperty(com.engine.util.Common.CROSSOVER_PROB_PROP));
        }
        double mutationProb = 1.0 / this.reader.getRules().size();
        if (properties.getProperty(com.engine.util.Common.MUTATION_PROB_PROP) != null) {
            mutationProb = Double.parseDouble(properties.getProperty(com.engine.util.Common.MUTATION_PROB_PROP));
        }

        int tournamentSize = 2;
        if (properties.getProperty(com.engine.util.Common.TOURNAMENT_SIZE_PROP) != null) {
            tournamentSize = Integer.parseInt(properties.getProperty(com.engine.util.Common.TOURNAMENT_SIZE_PROP));
        }

        // Set weight for CEG penalty
        UtilStats.setCEGPenalties(properties);

        // Second create the com.engine.algorithm
        IntegerFlipMutation<Variable<Integer>> mutationOperator = new IntegerFlipMutation<>(this, mutationProb);
        SinglePointCrossover<Variable<Integer>> crossoverOperator = new SinglePointCrossover<>(this, SinglePointCrossover.DEFAULT_FIXED_CROSSOVER_POINT, crossOverProb, SinglePointCrossover.AVOID_REPETITION_IN_FRONT);
        SimpleDominance<Variable<Integer>> comparator = new SimpleDominance<>();
        TournamentSelect<Variable<Integer>> selectionOp = new TournamentSelect<>(tournamentSize, comparator);

        if (numObjectives == 2) {
            algorithm = new ModifiedNSGAII(this, Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), mutationOperator, crossoverOperator, selectionOp);
        } else {
            algorithm = new SimpleGeneticAlgorithm<>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp);
        }
        if (obs != null) {
            algorithm.addObserver(obs);
        }

        int numExecutions = 1;
        if (properties.getProperty(com.engine.util.Common.NUM_EXECUTIONS) != null) {
            numExecutions = Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_EXECUTIONS));
        }

        ArrayList<String> log = new ArrayList<>();

        int i = 0;
        stop = false;
        while (!stop && (i < numExecutions)) {
            logger.info("Run #" + i);
            logger.info("========");

            double startTime = new Date().getTime();
            algorithm.initialize();
            solutions = algorithm.execute();
            double time = (new Date().getTime() - startTime) / 1000;
            logger.info("Execution time: " + time + " seconds.");

            executionReport.clear();

            if (numObjectives == 2) {
                for (Solution s : solutions) {
                    executionReport.add(s.getObjective(0) + " " + s.getObjective(1) + ";" + this.generatePhenotype(s).toString() + ";" + time);
                }

            } else {
                executionReport.add(solutions.get(0).getObjective(0) + ";" + this.generatePhenotype(solutions.get(0)).toString() + ";" + time);

                // Just for interrupted executions:
                logger.info("@@;" + this.generatePhenotype(solutions.get(0)).toString());
            }

            for (String s : executionReport) {
                log.add(i + ";" + s);
            }

            i++;
        }

        System.out.flush();
        System.err.flush();

        logger.info("Execution report");
        logger.info("==================");
        logger.info("#Run;" + SymbolicRegressionGE.REPORT_HEADER);
        executionReport.clear();
        for (String s : log) {
            logger.info(s);
            executionReport.add(s);
        }
        obs.getLock().lock();
        Run newRun = runService.findByRunId(run.getId());
        newRun.setModel(this.getModel());
        Run.Status status = null;
        if(run.getDiagramData().getFinished()){
            status=Run.Status.FINISHED;
        }else if(run.getDiagramData().getFailed()){
            status=Run.Status.FAILED;
        }else if(run.getDiagramData().getStopped()){
            status=Run.Status.STOPPED;
        }
        newRun.setStatus(status);
        newRun.setModificationDate(new Timestamp(new Date().getTime()));
        runService.saveRun(newRun);
        obs.getLock().lock();

    }

    //Method to get the variables
    private static HashMap<String, Integer> getVariables(String[][] phenotype) {
        String[] lineVars = phenotype[0];

        HashMap<String, Integer> aux = new HashMap<>();
        for (int i = 1; i < lineVars.length; i++) {
            aux.put(lineVars[i], i);
        }
        return aux;
    }

    //Method to get the logger from class SimpleGeneticAlgorithm
    private static Logger GetSimpleGeneticAlgorithmLogger() {
        LogManager manager = LogManager.getLogManager();
        return manager.getLogger("jeco.core.algorithm.ga.SimpleGeneticAlgorithm");
    }

    /**
     * Opens the log population file to add the string that was passed as a
     * parameter.
     */
    private void addToLogFile(String str) {
        try {
            File file = new File(logPopulationOutputFile);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(str);
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.err.println("Error in log population file: " + ex.getLocalizedMessage());
        }
    }

    public Boolean getStop() {
        return stop;
    }

    public String getModel() {
        if (solutions != null && !solutions.isEmpty()) {
            return this.generatePhenotype(solutions.get(0)).toString();
        }
        return null;
    }

    private String[][] processExperimentDataTypeInfo(String info) {
        String[] infoSplit = info.split("\\r\\n");
        String[][] matrix = new String[infoSplit.length][];

        ArrayList<String> columnList = new ArrayList<>();

        String[] columns = infoSplit[0].split("\r\n");
        int index = 0;
        for (String column : columns[0].split(";")) {
            if (index == 0) {
                columnList.add("#Y");
            } else {
                columnList.add("X" + index);
            }
            index++;
        }
        matrix[0] = columnList.toArray(new String[0]);

        int count = 1;
        for (int i = 1; i < infoSplit.length; i++) {
            matrix[count++] = infoSplit[i].split(";");
        }
        return matrix;
    }
}
