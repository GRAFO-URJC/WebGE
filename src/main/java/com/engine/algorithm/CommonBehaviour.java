package com.engine.algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.engine.util.UtilStats;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.SaveDBService;
import com.gramevapp.web.service.rabbitmq.MQConfig;
import com.gramevapp.web.service.rabbitmq.ReportRabbitmqMessage;
import jeco.core.algorithm.Algorithm;
import jeco.core.algorithm.de.DifferentialEvolution;
import jeco.core.algorithm.ga.SimpleGeneticAlgorithm;
import jeco.core.algorithm.ge.SimpleGrammaticalEvolution;
import jeco.core.algorithm.moga.NSGAII;
import jeco.core.algorithm.moge.MultiObjectiveGrammaticalEvolution;
import jeco.core.algorithm.moge.Phenotype;
import jeco.core.algorithm.sge.AbstractGECommon;
import jeco.core.algorithm.sge.VariableArray;
import jeco.core.algorithm.sge.VariableList;
import jeco.core.operator.crossover.SinglePointCrossover;
import jeco.core.problem.Problem;
import jeco.core.problem.Solution;
import jeco.core.problem.Solutions;
import jeco.core.problem.Variable;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class CommonBehaviour<T  extends Variable<?>> {
	 private static Logger logger;
	    
	    protected int threadId;
	    protected Properties properties;

	    public static final String REPORT_HEADER = "Obj.;Model;Time";
	    public static ArrayList<String> executionReport = new ArrayList<>();
	    protected static int realDataCopied = 0;
	    
	    protected static String logPopulationOutputFile;

	    
	    // Binary masks for logging:
	    public static final int LOG_GENOTYPE_MASK = 1;
	    public static final int LOG_USED_GENES_MASK = 2;
	    public static final int LOG_FITNESS_MASK = 4;
	    public static final int LOG_PHENOTYPE_MASK = 8;
	    public static final int LOG_EVALUATION_MASK = 16;
	    
	    public static boolean stop;

    private static final char ID_FOR_PARAMS = 'w';
	    
	    private Algorithm<T> algorithm;

    protected String[][] func;
    private boolean failed = false;
    private HashMap<String, Integer> vars = new HashMap<>();

    private Solutions<T> solutions;
        //------------------

        private Algorithm<Variable<Double>> alg;
        private String objective;
        private boolean de = false;

        private RabbitTemplate rabbitTemplate;
        private List<String> parameters;

        private NumberFormatException numberFormatException = null;

        private IllegalArgumentException illegalArgumentException = null;

        private final SolutionDEGE bestSolution;

    public static final String NUM_GENERATIONS_PROP = "NumGenerations";
    public static final String MUTATION_FACTOR_DE_PROP = "MutationFactorDE";
    public static final String RECOMB_FACTOR_PROP = "RecombinationFactorDE";
    public static final String MODEL_PROP = "ModelDE";
    public static final String LOWER_BOUND_PROP = "LowerBoundDE";
    public static final String UPPER_BOUND_PROP = "UpperBoundDE";
    public static final String POPULATION_DE = "PopulationSizeDE";
        //------------------
    private AbstractGECommon<?> problem = null;

	
	public CommonBehaviour(Properties properties, int numObjectives, String objective, RabbitTemplate rabbitTemplate) {
		
		
        this.properties = properties;

        this.objective = objective;


        this.bestSolution = new SolutionDEGE();
        this.bestSolution.setCost(Double.MAX_VALUE);

        this.rabbitTemplate = rabbitTemplate;

	}

    static void addToLogFile(String str) {
        try {
            File file = new File("C:\\Users\\Marina\\Documents\\GitHub\\WebGE\\log.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
            writer.write(str);
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.err.println("Error in log population file: " + ex.getLocalizedMessage());
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
    public void evaluateWithDe(Solution<T> solution, Phenotype phenotype) {
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
    public void evaluateWithoutDe(Solution<T> solution, Phenotype phenotype) {
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
                System.out.println("IllegalArgumentException");

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
            System.out.println("NumberFormatException");
            failed = true;
            numberFormatException = e;
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

    private void addToExecutionReport(double time) {
        Solution sol =solutions.get(0);
        executionReport.add(solutions.get(0).getObjective(0) + ";" + problem.phenotypeToString(sol) + ";" + time);
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
                for (Solution s : solutions) {
                    executionReport.add(s.getObjective(0) + " " + s.getObjective(1) + ";" + problem.phenotypeToString(s) + ";" + time);
                }

            } else {
                addToExecutionReport(time);
                // Just for interrupted executions:
                Solution sol =solutions.get(0);
                logger.log(Level.INFO,"@@;{0}",problem.phenotypeToString(sol));
            }

            for (String s : executionReport) {
                log.add(i + 1 + ";" + s);
            }

            i++;
        }
    }
    private void handleFailed() {
        String log = "";
        if (illegalArgumentException != null) {
            logger.log(Level.INFO,"{0} Incorrect grammar",illegalArgumentException.toString());
            log = "IncorrectGrammar";
        }
        if (numberFormatException != null) {
            logger.log(Level.INFO,"{0} target duplicate in dataset",numberFormatException.toString());
            log = "Numberformatexception";
        }

        addToLogFile(log);


    }



    /*private void updateAlgorithmFromNumObjetives(int numObjectives, RunGeObserver obs, double mutationProb, double crossOverProb, int tournamentSize) {
        Problem p = problem;
        if (numObjectives == 2) { //+
            algorithm = new NSGAII<>(p, Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), mutationProb, crossOverProb, tournamentSize);
        } else { //Put all options for algorithms 0:SimpleGrammaticalEvolution, 1:StaticSimpleGeneticAlgoritm, 2:StaticWithPopRenovation
            algorithm = new SimpleGeneticAlgorithm<>(p, Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_INDIVIDUALS_PROP)), Integer.parseInt(properties.getProperty(com.engine.util.Common.NUM_GENERATIONS_PROP)), mutationProb, crossOverProb);
        }
        if (obs != null) {
            algorithm.addObserver(obs);
        }
    }*/

    /**
     * Method to run the GE algorithm with the provided properties.
     */
    public void runGE(RunGeObserver obs, String experimentDatatypeInfo, Run run, RunService runService, SaveDBService saveDBService, Algorithm alg, AbstractGECommon prob, int numObjectives) {
        if(isRunStopped(run.getId(), runService) || isRunCancelled(run.getId(), runService)) {
            stopExecution();
        }
        this.algorithm = alg;
        this.problem = prob;

        if (obs != null) {
            algorithm.addObserver(obs);
            System.out.println("Added observer");
        }

        System.out.println(algorithm);
        System.out.println(problem);

        logger = Logger.getLogger(problem.getClass().getName());
        // Load target data
        // TODO: NO distinguir entre training, validation y test.
        func = processExperimentDataTypeInfo(experimentDatatypeInfo);
        vars = getVariables(func);
        logger.setLevel(Level.ALL);

        int numVars = func[0].length-1;
        int entries = func.length-1;
        logger.log(Level.INFO,"Training set input variables: {0}",numVars);
        logger.log(Level.INFO,"Training set entries: {0}\n",entries);

        // Set weight for CEG penalty
        UtilStats.setCEGPenalties(properties);

        Run finalRun = run;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(isRunCancelled(finalRun.getId(), runService)){
                    logger.warning("Run cancelled.");
                    stopExecution();
                }
                if(isRunStopped(finalRun.getId(), runService)) {
                    logger.warning("Run stopped");
                    stopExecution();
                }
            }
        };
        Timer timer = new Timer("Timer");
        timer.scheduleAtFixedRate(task, 20000, 20000);

        try {
            //updateAlgorithmFromNumObjetives(numObjectives, obs, mutationProb, crossOverProb, tournamentSize);

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
            logger.log(Level.INFO,"#Run;{0}",CommonBehaviour.REPORT_HEADER);
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
            runService.saveRun(run); //save run status
            //saveDBService.saveRunAsync(run);


            // Only report if run wasn't cancelled before.
            if(!isRunCancelled(run.getId(), runService)) {
                ReportRabbitmqMessage message = new ReportRabbitmqMessage(run.getId(), null, "finish");
                rabbitTemplate.convertAndSend(MQConfig.EXCHANGE, MQConfig.REPORT_ROUTING_KEY, message);
            }
            obs.getLock().lock();
        } finally {
            timer.cancel();
        }
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
            Solution sol = solutions.get(0);
            return problem.phenotypeToString(sol);
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

    /**
     * Stops problem execution, called from mainWindow
     */
    public void stopExecution() {
        System.out.println("Stop");
        stop = true;
        algorithm.stopExection();
        logger.log(Level.INFO,">>>> GE STOPPED - {0}",Thread.currentThread().getName());
        if (this.de) {
            alg.stopExection();
            logger.log(Level.INFO,">>>> DE STOPPED - {0}",Thread.currentThread().getName());

        }
    }

	public static boolean hasToLog(Solutions<?> solutions, Solutions<?> current_Pop) {
		if(solutions.size() != current_Pop.size()) {
			return false;
		}
		return true;
	}

}
