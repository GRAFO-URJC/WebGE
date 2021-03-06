/*
 * To change this license header, choose License Headers in Project ExpProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.algorithm;

import com.engine.util.UtilStats;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.SaveDBService;
import jeco.core.algorithm.Algorithm;
import jeco.core.algorithm.de.DifferentialEvolution;
import jeco.core.algorithm.ge.SimpleGrammaticalEvolution;
import jeco.core.algorithm.moge.AbstractProblemGE;
import jeco.core.algorithm.moge.MultiObjectiveGrammaticalEvolution;
import jeco.core.algorithm.moge.Phenotype;
import jeco.core.operator.crossover.SinglePointCrossover;
import jeco.core.problem.Solution;
import jeco.core.problem.Solutions;
import jeco.core.problem.Variable;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.lang3.StringUtils;

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

    protected String[][] func;
    private HashMap<String, Integer> vars = new HashMap<>();
    private boolean failed = false;
    private NumberFormatException numberFormatException = null;

    private IllegalArgumentException illegalArgumentException = null;

    protected Properties properties;
    private Solutions<Variable<Integer>> solutions;
    private String logPopulationOutputFile;

    // Binary masks for logging:
    public static final int LOG_GENOTYPE_MASK = 1;
    public static final int LOG_USED_GENES_MASK = 2;
    public static final int LOG_FITNESS_MASK = 4;
    public static final int LOG_PHENOTYPE_MASK = 8;
    public static final int LOG_EVALUATION_MASK = 16;


    public static final String NUM_GENERATIONS_PROP = "NumGenerations";
    public static final String MUTATION_FACTOR_DE_PROP = "MutationFactorDE";
    public static final String RECOMB_FACTOR_PROP = "RecombinationFactorDE";
    public static final String MODEL_PROP = "ModelDE";
    public static final String LOWER_BOUND_PROP = "LowerBoundDE";
    public static final String UPPER_BOUND_PROP = "UpperBoundDE";
    public static final String POPULATION_DE = "PopulationSizeDE";

    private static final char ID_FOR_PARAMS = 'w';

    private Algorithm<Variable<Integer>> algorithm;

    private Algorithm alg;
    private boolean stop;
    public ArrayList<String> parameters;

    public static final String REPORT_HEADER = "Obj.;Model;Time";
    public List<String> executionReport = new ArrayList<>();

    private String objective;
    private boolean DE = false;


    public SolutionDEGE bestSolution;

    public SymbolicRegressionGE(Properties properties, int numObjectives, String objective, boolean DE) {
        super(properties.getProperty(com.engine.util.Common.BNF_PATH_FILE_PROP), numObjectives,
                Integer.parseInt(properties.getProperty(com.engine.util.Common.CHROMOSOME_LENGTH_PROP)),
                Integer.parseInt(properties.getProperty(com.engine.util.Common.MAX_WRAPS_PROP)),
                AbstractProblemGE.CODON_UPPER_BOUND_DEFAULT);

        this.properties = properties;
        this.objective = objective;
        if (this.properties.getProperty(com.engine.util.Common.SENSIBLE_INITIALIZATION) != null) { // Not initializated in properties
            this.setSensibleInitialization(true, Double.parseDouble(this.properties.getProperty(com.engine.util.Common.SENSIBLE_INITIALIZATION)));
        }
        this.DE = DE;
        bestSolution = new SolutionDEGE();
        bestSolution.setCost(Double.MAX_VALUE);
    }

    public void stopExecution() {
        stop = true;
        algorithm.stopExection();
        logger.log(Level.INFO,">>>> GE STOPPED - "+Thread.currentThread().getName());
        if (this.DE) {
            alg.stopExection();
            logger.log(Level.INFO,">>>> DE STOPPED - "+Thread.currentThread().getName());

        }
    }

    @Override
    public void evaluate(Solution<Variable<Integer>> solution, Phenotype phenotype) {

        if (DE) {

            // Parameters are stored in a list according to their position.
            // The number of elements to be tuned is the number of variables -> size of the list.
            // In addition, we control that repetition of parameters are not new parameters !
            HashSet<String> paramIds = new HashSet<>();
            parameters = new ArrayList<>();
            int i = 0;

            String model = phenotype.toString();
            while (i < model.length()) {
                if (model.charAt(i) == ID_FOR_PARAMS) {
                    // Parse element:
                    String id = ID_FOR_PARAMS + "";
                    i++;
                    while ((i < model.length()) && (Character.isDigit(model.charAt(i)))) {
                        id += model.charAt(i);
                        i++;
                    }
                    if (!paramIds.contains(id)) {
                        parameters.add(id);
                        paramIds.add(id);
                    }
                } else {
                    i++;
                }
            }

            ProblemDE problem = new ProblemDE(parameters.size(),
                    Double.valueOf(properties.getProperty(LOWER_BOUND_PROP)),
                    Double.valueOf(properties.getProperty(UPPER_BOUND_PROP)), objective, func, phenotype.toString(), parameters);

            // Optimize model with DE
            alg = new DifferentialEvolution(problem,
                    Integer.valueOf(properties.getProperty(POPULATION_DE)),
                    Integer.valueOf(properties.getProperty(NUM_GENERATIONS_PROP)),
                    true,
                    Double.valueOf(properties.getProperty(MUTATION_FACTOR_DE_PROP)),
                    Double.valueOf(properties.getProperty(RECOMB_FACTOR_PROP)));
            try {
                alg.initialize();
            } catch (Exception e) {
                logger.info(e.toString());
            }


            if (!stop) {
                Solution<Variable<?>> best = (Solution<Variable<?>>) alg.execute().get(0);
                // Store objective
                double obj = best.getObjective(0);
                solution.getObjectives().set(0, obj);


                if (obj < bestSolution.getCost()) {
                    bestSolution.setCost(obj);
                    bestSolution.setModel(phenotype.toString());
                    HashMap<String, Double> parameterValues = new HashMap<>(best.getVariables().size());
                    // Include the values of the parameters:
                    for (int j = 0; j < best.getVariables().size(); j++) {
                        parameterValues.put(parameters.get(j), (Double) best.getVariable(j).getValue());
                    }
                    bestSolution.setParameterValues(parameterValues);
                }
            }

        } else {
            String originalFunction = phenotype.toString();

            //Create array of prediction
            String[] prediction = new String[func.length];
            prediction[0] = originalFunction;

            //Evaluation from phenotype
            for (int i = 1; i < func.length; i++) {
                String currentFunction = calculateFunctionValued(originalFunction, i);
                Double funcI;

                Expression e = new ExpressionBuilder(currentFunction).build();
                try {
                    funcI = e.evaluate();
                    if (funcI.isNaN()) {
                        funcI = Double.POSITIVE_INFINITY;
                    }
                } catch (IllegalArgumentException ex) {
                    illegalArgumentException = ex;
                    failed = true;
                    this.stopExecution();
                    funcI = Double.POSITIVE_INFINITY;
                }
                //Add to prediction array the evaluation calculated
                prediction[i] = String.valueOf(funcI);
                solution.getProperties().put(String.valueOf(i), funcI);
            }

            try {
                double fValue = ModelEvaluator.calculateObjective(func, prediction, objective);
                if (Double.isNaN(fValue)) {
                    solution.getObjectives().set(0, Double.POSITIVE_INFINITY);
                } else {
                    solution.getObjectives().set(0, fValue);

                }
            } catch (NumberFormatException e) {
                failed = true;
                numberFormatException = e;
            }
        }
    }

    public HashMap<String, Double> obtainParameterValues(Solution<Variable<?>> sol) {
        HashMap<String, Double> parameterValues = new HashMap<>(sol.getVariables().size());
        // Include the values of the parameters:
        for (int j = 0; j < sol.getVariables().size(); j++) {
            parameterValues.put(parameters.get(j), (Double) sol.getVariable(j).getValue());
        }

        return parameterValues;
    }


    //Method to replace the unknowns variables by values
    private String calculateFunctionValued(String originalFunction, int index) {
        String newFunction = originalFunction;

        // TODO: unify this function and the other one with same idea
        String key;
        for (int i = vars.size(); i > 0; i--) {
            key = "X" + i;
            int keyPosition = vars.get(key);
            newFunction = StringUtils.replace(newFunction, key, func[index][keyPosition]);
        }
        return newFunction;
    }


    //Method to replace the unknowns variables by values
    public static Double calculateFunctionValuedResultWithCSVData(String originalFunction, String[] content) throws IllegalArgumentException {
        String newFunction = originalFunction;


        String replacePart;
        for (int i = (content.length - 1); i > 0; i--) {
            replacePart = "X" + i;
            newFunction = newFunction.replaceAll(replacePart, content[i]);
        }


        Expression e = new ExpressionBuilder(newFunction).build();
        return e.evaluate();

    }

    @Override
    public SymbolicRegressionGE clone() {
        return new SymbolicRegressionGE(properties, this.numberOfObjectives, objective, DE);
    }


    /**
     * Method to run the GE algorithm with the provided properties.
     */
    public void runGE(RunGeObserver obs, String experimentDatatypeInfo, Run run, RunService runService, SaveDBService saveDBService) {
        // Load target data
        // TODO: NO distinguir entre training, validation y test.
        func = processExperimentDataTypeInfo(experimentDatatypeInfo);
        vars = getVariables(func);
        logger.setLevel(Level.ALL);

        logger.log(Level.INFO,"Training set input variables: "+(func[0].length-1));
        logger.log(Level.INFO,"Training set entries: "+(func.length-1)+"\n");

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
            //addToLogFile(buffer.toString());
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

        if (numObjectives == 2) {
            algorithm = new MultiObjectiveGrammaticalEvolution(this, Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), mutationProb, crossOverProb, tournamentSize);
        } else {
            algorithm = new SimpleGrammaticalEvolution(this, Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), mutationProb, crossOverProb);
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

            double startTime = new Date().getTime();
            try {
                algorithm.initialize();
            } catch (Exception e) {
                logger.info(e.toString() + " Incorrect grammar");
                run = runService.findByRunId(run.getId());
                run.setStatus(Run.Status.FAILED);
                saveDBService.saveRunAsync(run);
                return;
            }
            solutions = algorithm.execute();
            if (failed) {
                if (illegalArgumentException != null) {
                    logger.info(illegalArgumentException.toString() + " Incorrect grammar");
                }
                if (numberFormatException != null) {
                    logger.info(numberFormatException.toString() + ", target duplicate in dataset");
                }
            }
            logger.info("Run #" + i);
            logger.info("========");
            double time = (new Date().getTime() - startTime) / 1000;
            logger.info("Execution time: " + time + " seconds.");

            executionReport.clear();

            if (numObjectives == 2) {
                for (Solution s : solutions) {
                    executionReport.add(s.getObjective(0) + " " + s.getObjective(1) + ";" + this.generatePhenotype(s).toString() + ";" + time);
                }

            } else {
                executionReport.add(solutions.get(0).getObjective(0) + ";" + this.generatePhenotype(solutions.get(0)).toString() + ";" + time);
                if (bestSolution.getModel() != null) {
                    for (int j = 0; j <= (this.vars.size()); j++) {
                        String currentWeight = "w" + j;
                        if (bestSolution.getParameterValues().get(currentWeight) != null) {

                            executionReport.add(currentWeight + "= " + bestSolution.getParameterValues().get(currentWeight) + ";");
                        }
                    }

                }
                // Just for interrupted executions:
                logger.info("@@;" + this.generatePhenotype(solutions.get(0)).toString());
            }

            for (String s : executionReport) {
                log.add(i + 1 + ";" + s);
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
        run = runService.findByRunId(run.getId());

        String model = this.getModel();
        String replacePart;
        if (bestSolution.getModel() != null) {
            for (int j = (this.vars.size()); j >= 0; j--) {
                replacePart = "w" + j;
                model = model.replaceAll(replacePart, String.valueOf(bestSolution.getParameterValues().get(replacePart)));
            }

        }

        run.setModel(model);


        if (failed) {
            run.setStatus(Run.Status.FAILED);
        } else if (run.getStatus() != null && !run.getStatus().equals(Run.Status.STOPPED)) {
            run.setStatus(Run.Status.FINISHED);
        }
        run.setModificationDate(new Timestamp(new Date().getTime()));
        saveDBService.saveRunAsync(run);
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
    private static Logger getSimpleGeneticAlgorithmLogger() {
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
            logger.warning("Error in log population file: " + ex.getLocalizedMessage());
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
