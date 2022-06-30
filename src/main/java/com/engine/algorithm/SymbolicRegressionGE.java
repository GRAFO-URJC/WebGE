/*
 * To change this license header, choose License Headers in Project ExpProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.engine.algorithm;

import com.engine.util.UtilStats;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.rabbitmq.MQConfig;
import com.gramevapp.web.service.rabbitmq.ReportRabbitmqMessage;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private Algorithm<Variable<Double>> alg;
    private boolean stop;
    private List<String> parameters;

    public static final String REPORT_HEADER = "Obj.;Model;Time";
    private final List<String> executionReport = new ArrayList<>();

    private String objective;
    private boolean de = false;

    private RabbitTemplate rabbitTemplate;


    private final SolutionDEGE bestSolution;

    public SymbolicRegressionGE(Properties properties, int numObjectives, String objective, boolean de, RabbitTemplate rabbitTemplate) {
        super(properties.getProperty(com.engine.util.Common.BNF_PATH_FILE_PROP), numObjectives,
                Integer.parseInt(properties.getProperty(com.engine.util.Common.CHROMOSOME_LENGTH_PROP)),
                Integer.parseInt(properties.getProperty(com.engine.util.Common.MAX_WRAPS_PROP)),
                AbstractProblemGE.CODON_UPPER_BOUND_DEFAULT);

        this.properties = properties;
        this.objective = objective;
        if (this.properties.getProperty(com.engine.util.Common.SENSIBLE_INITIALIZATION) != null) { // Not initializated in properties
            this.setSensibleInitialization(true, Double.parseDouble(this.properties.getProperty(com.engine.util.Common.SENSIBLE_INITIALIZATION)));
        }
        this.de = de;
        bestSolution = new SolutionDEGE();
        bestSolution.setCost(Double.MAX_VALUE);

        this.rabbitTemplate = rabbitTemplate;
    }

    public void stopExecution() {
        stop = true;
        algorithm.stopExection();
        logger.log(Level.INFO,">>>> GE STOPPED - {0}",Thread.currentThread().getName());
        if (this.de) {
            alg.stopExection();
            logger.log(Level.INFO,">>>> DE STOPPED - {0}",Thread.currentThread().getName());

        }
    }

    // Method used for refactoring evaluate()
    private void iterateModel(String model, Set<String> paramIds) {
        int i = 0;

        while (i < model.length()) {
            if (model.charAt(i) == ID_FOR_PARAMS) {
                // Parse element:
                StringBuilder id = new StringBuilder(ID_FOR_PARAMS + "");
                i++;
                while ((i < model.length()) && (Character.isDigit(model.charAt(i)))) {
                    id.append(model.charAt(i));
                    i++;
                }
                if (!paramIds.contains(id.toString())) {
                    parameters.add(id.toString());
                    paramIds.add(id.toString());
                }
            } else {
                i++;
            }
        }
    }

    // Method used for refactoring evaluate()
    private void evaluateWithDe(Solution<Variable<Integer>> solution, Phenotype phenotype) {
        // Parameters are stored in a list according to their position.
        // The number of elements to be tuned is the number of variables -> size of the list.
        // In addition, we control that repetition of parameters are not new parameters !
        HashSet<String> paramIds = new HashSet<>();
        parameters = new ArrayList<>();
        String model = phenotype.toString();

        // func
        iterateModel(model, paramIds);

        ProblemDE problem = new ProblemDE(parameters.size(),
                Double.parseDouble(properties.getProperty(LOWER_BOUND_PROP)),
                Double.parseDouble(properties.getProperty(UPPER_BOUND_PROP)), objective, func, phenotype.toString(), parameters);

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
            Solution<Variable<Double>> best = alg.execute().get(0);
            // Store objective
            double obj = best.getObjective(0);
            solution.getObjectives().set(0, obj);


            if (obj < bestSolution.getCost()) {
                bestSolution.setCost(obj);
                bestSolution.setModel(phenotype.toString());
                HashMap<String, Double> parameterValues = new HashMap<>(best.getVariables().size());
                // Include the values of the parameters:
                for (int j = 0; j < best.getVariables().size(); j++) {
                    parameterValues.put(parameters.get(j), best.getVariable(j).getValue());
                }
                bestSolution.setParameterValues(parameterValues);
            }
        }
    }

    // Method used for refactoring evaluate()
    private void evaluateWithoutDe(Solution<Variable<Integer>> solution, Phenotype phenotype) {
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

    @Override
    public void evaluate(Solution<Variable<Integer>> solution, Phenotype phenotype) {
        if(de) {
            evaluateWithDe(solution, phenotype);
        } else {
            evaluateWithoutDe(solution, phenotype);
        }
    }

    public Map<String, Double> obtainParameterValues(Solution<Variable<?>> sol) {
        Map<String, Double> parameterValues = new HashMap<>(sol.getVariables().size());
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
        return new SymbolicRegressionGE(properties, this.numberOfObjectives, objective, de, rabbitTemplate);
    }

    private void addToExecutionReport(double time) {
        executionReport.add(solutions.get(0).getObjective(0) + ";" + this.generatePhenotype(solutions.get(0)).toString() + ";" + time);
        if (bestSolution.getModel() != null) {
            for (int j = 0; j <= (this.vars.size()); j++) {
                String currentWeight = "w" + j;
                if (bestSolution.getParameterValues().get(currentWeight) != null) {
                    executionReport.add(currentWeight + "= " + bestSolution.getParameterValues().get(currentWeight) + ";");
                }
            }
        }
    }

    private void startExecutions(int numExecutions, Run run, SaveDBService saveDBService, RunService runService, int numObjectives, List<String> log) {
        int i = 0;
        stop = false;
        while (!stop && (i < numExecutions)) {

            double startTime = new Date().getTime();
            try {
                algorithm.initialize();
            } catch (Exception e) {
                logger.log(Level.INFO,"{0} Incorrect grammar",e.toString());
                run = runService.findByRunId(run.getId());
                run.setStatus(Run.Status.FAILED);
                saveDBService.saveRunAsync(run);
                return;
            }
            solutions = algorithm.execute();
            if (failed) {
                handleFailed();
            }
            logger.log(Level.INFO,"Run #{0}", i);
            logger.info("========");
            double time = (new Date().getTime() - startTime) / 1000;
            logger.log(Level.INFO,"Execution time: {0} seconds.",time);

            executionReport.clear();

            if (numObjectives == 2) {
                for (Solution<Variable<Integer>> s : solutions) {
                    executionReport.add(s.getObjective(0) + " " + s.getObjective(1) + ";" + this.generatePhenotype(s).toString() + ";" + time);
                }

            } else {
                addToExecutionReport(time);
                // Just for interrupted executions:
                logger.log(Level.INFO,"@@;{0}",this.generatePhenotype(solutions.get(0)));
            }

            for (String s : executionReport) {
                log.add(i + 1 + ";" + s);
            }

            i++;
        }
    }

    private void handleFailed() {
        if (illegalArgumentException != null) {
            logger.log(Level.INFO,"{0} Incorrect grammar",illegalArgumentException.toString());
        }
        if (numberFormatException != null) {
            logger.log(Level.INFO,"{0} target duplicate in dataset",numberFormatException.toString());
        }
    }

    private void updateAlgorithmFromNumObjetives(int numObjectives, RunGeObserver obs, double mutationProb, double crossOverProb, int tournamentSize) {
        if (numObjectives == 2) { //+
            algorithm = new MultiObjectiveGrammaticalEvolution(this, Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), mutationProb, crossOverProb, tournamentSize);
        } else {
            algorithm = new SimpleGrammaticalEvolution(this, Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), mutationProb, crossOverProb);
        }
        if (obs != null) {
            algorithm.addObserver(obs);
        }
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

        int numVars = func[0].length-1;
        int entries = func.length-1;
        logger.log(Level.INFO,"Training set input variables: {0}",numVars);
        logger.log(Level.INFO,"Training set entries: {0}\n",entries);

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

        updateAlgorithmFromNumObjetives(numObjectives, obs, mutationProb, crossOverProb, tournamentSize);

        int numExecutions = 1;
        if (properties.getProperty(com.engine.util.Common.NUM_EXECUTIONS) != null) {
            numExecutions = Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_EXECUTIONS));
        }

        ArrayList<String> log = new ArrayList<>();

        startExecutions(numExecutions, run, saveDBService, runService, numObjectives, log);

        System.out.flush();
        System.err.flush();

        logger.info("Execution report");
        logger.info("==================");
        logger.log(Level.INFO,"#Run;{0}",SymbolicRegressionGE.REPORT_HEADER);
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
        //saveDBService.saveRunAsync(run);

        // Only report if run wasn't cancelled before.
        if(!isRunCancelled(run.getId(), runService)) {
            ReportRabbitmqMessage message = new ReportRabbitmqMessage(run.getId(), null, "finish");
            rabbitTemplate.convertAndSend(MQConfig.EXCHANGE, MQConfig.REPORT_ROUTING_KEY, message);
        }
        obs.getLock().lock();
    }

    private boolean isRunCancelled(Long runId, RunService runService) {
        return runService.findByRunId(runId).getStatus().equals(Run.Status.CANCELLED);
    }

    private boolean isRunStopped(Long runId,  RunService runService) {
        return runService.findByRunId(runId).getStatus().equals(Run.Status.STOPPED);
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

        for (int index = 0, i = 0; i < columns[0].split(";").length; i++, index++) {
            if (index == 0) {
                columnList.add("#Y");
            } else {
                columnList.add("X" + index);
            }
        }
        matrix[0] = columnList.toArray(new String[0]);

        int count = 1;
        for (int i = 1; i < infoSplit.length; i++) {
            matrix[count++] = infoSplit[i].split(";");
        }
        return matrix;
    }
}
