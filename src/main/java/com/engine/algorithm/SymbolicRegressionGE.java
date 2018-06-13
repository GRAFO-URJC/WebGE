/*
 * To change this license header, choose License Headers in Project ExpProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.algorithm;

import com.engine.util.CSVReader;
import com.engine.util.Common;
import com.engine.util.UtilStats;
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

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static com.engine.util.Common.currentDateTimeAsFormattedString;

/**
 *
 * @author Carlos García Moreno, J. M. Colmenar
 */
public class SymbolicRegressionGE extends AbstractProblemGE {

    private static final Logger logger = Logger.getLogger(SymbolicRegressionGE.class.getName());
    protected Evaluator evaluator;
    protected static String[][] func;
    private static HashMap<String, Integer> vars = new HashMap<String, Integer>();

    protected Properties properties;
    private static int logPopulation = 0;
    private static String logPopulationOutputFile;
    private static int gen;

    // Binary masks for logging:
    public static final int LOG_GENOTYPE_MASK = 1;
    public static final int LOG_USED_GENES_MASK = 2;
    public static final int LOG_FITNESS_MASK = 4;
    public static final int LOG_PHENOTYPE_MASK = 8;
    public static final int LOG_EVALUATION_MASK = 16;

    private static Algorithm<Variable<Integer>> algorithm;
    private static boolean stop;

    public static final String REPORT_HEADER = "Obj.;Model;Time";
    public static ArrayList<String> executionReport = new ArrayList<>();


    public SymbolicRegressionGE(Properties properties, int numObjectives) {
        super(properties.getProperty(com.engine.util.Common.BNF_PATH_FILE_PROP), numObjectives,
                Integer.valueOf(properties.getProperty(com.engine.util.Common.CHROMOSOME_LENGTH_PROP)),
                Integer.valueOf(properties.getProperty(com.engine.util.Common.MAX_WRAPS_PROP)),
                AbstractProblemGE.CODON_UPPER_BOUND_DEFAULT);

        this.properties = properties;

        if (this.properties.getProperty(com.engine.util.Common.SENSIBLE_INITIALIZATION) != null) { // Not initializated in properties
            this.setSensibleInitialization(true,Double.valueOf(this.properties.getProperty(com.engine.util.Common.SENSIBLE_INITIALIZATION)));
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
                    funcI = Double.valueOf(aux);
                }
            } catch (EvaluationException ex) {
                Logger.getLogger(SymbolicRegressionGE.class.getName()).log(Level.SEVERE, null, ex);
                funcI = Double.POSITIVE_INFINITY;
            }
            //Add to prediction array the evaluation calculated
            prediction[i] = String.valueOf(funcI);
            solution.getProperties().put(String.valueOf(i), (double) funcI);
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

        Iterator iterator = vars.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            String key = pair.getKey().toString().toUpperCase();
            int keyPosition = Integer.parseInt(pair.getValue().toString());
            newFunction = newFunction.replace(key, func[index][keyPosition]);
        }
        return newFunction;
    }

    @Override
    public SymbolicRegressionGE clone() {
        SymbolicRegressionGE clone = new SymbolicRegressionGE(properties,this.numberOfObjectives);
        return clone;
    }


    /**
     * Method to run the GE algorithm with the provided properties.
     *
     * @param obs
     */
    public void runGE(RunGeObserver obs) {
        // Load target data
        // TODO: NO distinguir entre training, validation y test.
        CSVReader csv = new CSVReader(properties.getProperty(Common.TRAINING_PATH_PROP));
        try {
            func = csv.loadMatrix();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        vars = getVariables(func);
        logger.setLevel(Level.ALL);

        // Log population:
        logPopulation = Integer.valueOf(properties.getProperty(com.engine.util.Common.LOG_POPULATION_PROP));
        if (logPopulation > 0) {
            gen = 0;
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
                && (Integer.valueOf(properties.getProperty(com.engine.util.Common.OBJECTIVES_PROP)) == 2)) {
            numObjectives = 2;
        }

        // Adjust some properties
        double crossOverProb = SinglePointCrossover.DEFAULT_PROBABILITY;
        if (properties.getProperty(com.engine.util.Common.CROSSOVER_PROB_PROP) != null) {
            crossOverProb = Double.valueOf(properties.getProperty(com.engine.util.Common.CROSSOVER_PROB_PROP));
        }
        double mutationProb = 1.0 / this.reader.getRules().size();
        if (properties.getProperty(com.engine.util.Common.MUTATION_PROB_PROP) != null) {
            mutationProb = Double.valueOf(properties.getProperty(com.engine.util.Common.MUTATION_PROB_PROP));
        }

        int tournamentSize = 2;
        if (properties.getProperty(com.engine.util.Common.TOURNAMENT_SIZE_PROP) != null) {
            tournamentSize = Integer.valueOf(properties.getProperty(com.engine.util.Common.TOURNAMENT_SIZE_PROP));
        }

        // Set weight for CEG penalty
        UtilStats.setCEGPenalties(properties);

        // Second create the com.engine.algorithm
        IntegerFlipMutation<Variable<Integer>> mutationOperator = new IntegerFlipMutation<>(this, mutationProb);
        SinglePointCrossover<Variable<Integer>> crossoverOperator = new SinglePointCrossover<>(this, SinglePointCrossover.DEFAULT_FIXED_CROSSOVER_POINT, crossOverProb, SinglePointCrossover.AVOID_REPETITION_IN_FRONT);
        SimpleDominance<Variable<Integer>> comparator = new SimpleDominance<>();
        TournamentSelect<Variable<Integer>> selectionOp = new TournamentSelect<>(tournamentSize,comparator);

        if (numObjectives == 2) {
            algorithm = new ModifiedNSGAII(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), mutationOperator, crossoverOperator, selectionOp);
        } else {
            algorithm = new SimpleGeneticAlgorithm<>(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp);
        }
        if (obs != null) {
            algorithm.addObserver(obs);
        }

        int numExecutions = 1;
        if (properties.getProperty(com.engine.util.Common.NUM_EXECUTIONS) != null) {
            numExecutions = Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_EXECUTIONS));
        }

        ArrayList<String> log = new ArrayList<>();

        int i=0;
        stop = false;
        while (!stop && (i < numExecutions)) {
            System.out.println("\nRun #" + i);
            System.out.println("========");

            double startTime = System.currentTimeMillis();
            algorithm.initialize();
            Solutions<Variable<Integer>> solutions = algorithm.execute();
            double time = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("\nExecution time: " + time + " seconds.\n");

            executionReport.clear();

            if (numObjectives == 2) {
                for (Solution s : solutions) {
                    executionReport.add(String.valueOf(s.getObjective(0)) + " " + String.valueOf(s.getObjective(1)) + ";" + this.generatePhenotype(s).toString() + ";" + String.valueOf(time));
                }

            } else {
                executionReport.add(String.valueOf(solutions.get(0).getObjective(0)) + ";" + this.generatePhenotype(solutions.get(0)).toString() + ";" + String.valueOf(time));

                // Just for interrupted executions:
                System.out.println("\n\n@@;" + this.generatePhenotype(solutions.get(0)).toString() + "\n\n");
            }

            for (String s : SymbolicRegressionGE.executionReport) {
                log.add(i + ";" + s);
            }

            i++;
        }

        System.out.flush();
        System.err.flush();

        System.out.println("\n\n Execution report");
        System.out.println("==================\n");
        System.out.println("#Run;" + SymbolicRegressionGE.REPORT_HEADER);
        SymbolicRegressionGE.executionReport.clear();
        for (String s : log) {
            System.out.println(s);
            SymbolicRegressionGE.executionReport.add(s);
        }


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
     *
     * @param str
     */
    private static void addToLogFile(String str) {
        try {
            File file = new File(logPopulationOutputFile);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
            writer.write(str);
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.err.println("Error in log population file: " + ex.getLocalizedMessage());
        }
    }

    public Boolean getStop(){
        return stop;
    }
}
