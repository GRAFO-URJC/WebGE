package com.engine.algorithm;

import static com.engine.data.Common.currentDateTimeAsFormattedString;
import com.engine.data.NormalizedDataTable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observer;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
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
import com.engine.util.MyCompiler;
import com.engine.util.MyLoader;
import com.engine.util.UtilStats;

public class GramEvalTemporalModel extends AbstractProblemGE {

    private static final Logger logger = Logger.getLogger(GramEvalTemporalModel.class.getName());

    private static Algorithm<Variable<Integer>> algorithm;
    private static GramEvalTemporalModel problem = null;
    private static boolean stop;
    
    protected int threadId;
    protected MyCompiler compiler;
    protected NormalizedDataTable dataTable;
    protected Properties properties;
    protected AbstractPopEvaluator evaluator;

    public static final String REPORT_HEADER = "Obj.;Model;Time";
    public static ArrayList<String> executionReport = new ArrayList<>();
    protected static int realDataCopied = 0;
    
    private static int logPopulation = 0;
    private static String logPopulationOutputFile;
    private static int gen;
    
    // Binary masks for logging:
    public static final int LOG_GENOTYPE_MASK = 1;
    public static final int LOG_USED_GENES_MASK = 2;
    public static final int LOG_FITNESS_MASK = 4;
    public static final int LOG_PHENOTYPE_MASK = 8;
    public static final int LOG_EVALUATION_MASK = 16;
    
    public GramEvalTemporalModel(Properties properties, int threadId, int numObjectives) throws IOException {
        super(properties.getProperty(com.engine.data.Common.BNF_PATH_FILE_PROP), numObjectives,
              Integer.valueOf(properties.getProperty(com.engine.data.Common.CHROMOSOME_LENGTH_PROP)),
              Integer.valueOf(properties.getProperty(com.engine.data.Common.MAX_WRAPS_PROP)),
              AbstractProblemGE.CODON_UPPER_BOUND_DEFAULT);
        this.properties = properties;
        this.threadId = threadId;
        compiler = new MyCompiler(properties,logger);
        boolean normalizeData = false;
        if (properties.getProperty(com.engine.data.Common.NORMALIZED_DATA_PROP) != null) {
            normalizeData = Boolean.valueOf(properties.getProperty(com.engine.data.Common.NORMALIZED_DATA_PROP));
        } 
        
        if (this.properties.getProperty(com.engine.data.Common.REAL_DATA_COPIED_PROP) != null) {
            realDataCopied = Integer.valueOf(this.properties.getProperty(com.engine.data.Common.REAL_DATA_COPIED_PROP));
        } else {
            realDataCopied = 0;
        }
        
        if (this.properties.getProperty(com.engine.data.Common.SENSIBLE_INITIALIZATION) != null) { // Not initializated in properties
            this.setSensibleInitialization(true,Double.valueOf(this.properties.getProperty(com.engine.data.Common.SENSIBLE_INITIALIZATION)));
        }
        
        dataTable = new NormalizedDataTable(this, properties.getProperty(com.engine.data.Common.TRAINING_PATH_PROP),
                properties.getProperty(com.engine.data.Common.VALIDATION_PATH_PROP),
                Double.valueOf(properties.getProperty(com.engine.data.Common.ERROR_THRESHOLD_PROP)),
                Boolean.valueOf(properties.getProperty(com.engine.data.Common.VIEW_RESULTS_PROP)),normalizeData,
                Integer.valueOf(properties.getProperty(com.engine.data.Common.MODEL_WIDTH_PROP))+realDataCopied);
    }

    public boolean generateAndCompileEvaluationFile(ArrayList<String> exprList) {
        StringBuilder currentJavaFile = new StringBuilder();

        currentJavaFile.append("public class PopEvaluator").append(threadId).append(" extends com.engine.algorithm.AbstractPopEvaluator {\n\n");

        currentJavaFile.append("\tpublic double evaluate(int idxExpr, int k) {\n");
        currentJavaFile.append("\t\tdouble result = 0.0;\n\n");
//        currentJavaFile.append("\t\ttry {\n");
/*
        currentJavaFile.append("\t\t// Target glucose test:\n");
        currentJavaFile.append("\t\tif (getVariable(timeTable.get(0).length - 2, k) < 0) {\n");
        currentJavaFile.append("\t\t\treturn realData(k);\n");
        currentJavaFile.append("\t\t}\n\n");
*/
        currentJavaFile.append("\t\t\tswitch(idxExpr) {\n");
        for (int i = 0; i < exprList.size(); ++i) {
            currentJavaFile.append("\t\t\t\tcase ").append(i).append(":\n");
            currentJavaFile.append("\t\t\t\t\tresult = ").append(exprList.get(i)).append(";\n");
            currentJavaFile.append("\t\t\t\t\tbreak;\n");
        }
        currentJavaFile.append("\t\t\t\tdefault:\n");
        currentJavaFile.append("\t\t\t\t\tresult = Double.POSITIVE_INFINITY;\n");
        currentJavaFile.append("\t\t\t}\n"); // End switch

//        currentJavaFile.append("\t\t}\n"); // End try
//        currentJavaFile.append("\t\tcatch (Exception ee) {\n");
//        currentJavaFile.append("\t\t\t// System.err.println(ee.getLocalizedMessage());\n");
//        currentJavaFile.append("\t\t\tresult = Double.POSITIVE_INFINITY;\n");
//        currentJavaFile.append("\t\t}\n"); // End catch
        currentJavaFile.append("\t\tif(Double.isNaN(result)) {\n");
        currentJavaFile.append("\t\t\tresult = Double.POSITIVE_INFINITY;\n");
        currentJavaFile.append("\t\t}\n");
        currentJavaFile.append("\t\treturn result;\n");
        currentJavaFile.append("\t}\n");
        currentJavaFile.append("}\n");
        
        // Compilation process:
        boolean compilationOK = false;
        File file = null;
        try {
            file = new File(compiler.getWorkDir() + File.separator + "PopEvaluator" + threadId + ".java");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(currentJavaFile.toString());
            writer.flush();
            writer.close();
            LinkedList<String> filePaths = new LinkedList<>();
            filePaths.add(file.getAbsolutePath());
            compilationOK = compiler.compile(filePaths);
            if (!compilationOK) {
                logger.severe("Unable to compile, with errors:");
                logger.severe(compiler.getOutput());
            }
        } catch (IOException ex) {
            logger.severe("Problem with compilation of "+file.getAbsolutePath()+": "+ex.getLocalizedMessage());
        }
        
        return compilationOK;
    }
    
    
    @Override
    public void evaluate(Solutions<Variable<Integer>> solutions) {
        ArrayList<String> solStrings = new ArrayList<>();
        
        for (int i = 0; i < solutions.size(); ++i) {
            Solution<Variable<Integer>> solution = solutions.get(i);
            Phenotype phenotype = generatePhenotype(solution);
            if (correctSol) {
                solStrings.add(phenotype.toString());
            } else {
                solStrings.add("Double.POSITIVE_INFINITY");
            }
        }                
                        
        if (generateAndCompileEvaluationFile(solStrings)) {
            // And now we evaluate all the solutions with the compiled file:
            evaluator = null;
            try {
                evaluator = (AbstractPopEvaluator) (new MyLoader(compiler.getWorkDir())).loadClass("PopEvaluator" + threadId).newInstance();
                evaluator.setTimeTable(dataTable.getTrainingTable());
                if (this.properties.getProperty(com.engine.data.Common.REAL_DATA_COPIED_PROP) != null) {
                    evaluator.setRealDataCopied(Integer.valueOf(this.properties.getProperty(com.engine.data.Common.REAL_DATA_COPIED_PROP)));
                }
                if (this.properties.getProperty(com.engine.data.Common.MODEL_WIDTH_PROP) != null) {
                    evaluator.setModelWidth(Integer.valueOf(this.properties.getProperty(com.engine.data.Common.MODEL_WIDTH_PROP)));
                }                    
            } catch (Exception ex) {
                logger.severe(ex.getLocalizedMessage());
            }
            
            StringBuilder buffer = null;
            if (logPopulation > 0) {
                buffer = new StringBuilder();
                buffer.append("\nGeneration ").append(gen).append("\n");
                gen++;
                addToLogFile(buffer.toString());
            }        
            
            for (int i = 0; i < solutions.size(); ++i) {
                Solution<Variable<Integer>> solution = solutions.get(i);
                if (super.numberOfObjectives == 2) {
                    NormalizedDataTable.OBJECTIVE = NormalizedDataTable.OBJ_RMSE;
                    solution.getObjectives().set(0, dataTable.evaluate(evaluator, solution, i));
                    NormalizedDataTable.OBJECTIVE = NormalizedDataTable.OBJ_CEG;
                    solution.getObjectives().set(1, dataTable.evaluate(evaluator, solution, i));
                } else {
                    NormalizedDataTable.OBJECTIVE = Integer.valueOf(this.properties.getProperty(com.engine.data.Common.OBJECTIVES_PROP));
                    double fitness = dataTable.evaluate(evaluator, solution, i);
                    if (Double.isNaN(fitness)) {
//                        logger.info("I have a NaN number here: "+problem.generatePhenotype(solutions.get(0)).toString());
                        fitness = Double.MAX_VALUE;
                    }
                    solution.getObjectives().set(0, fitness);
                }
                
                // If logSolutions, write to file ...
                if (logPopulation > 0) {
                    Solution<Variable<Integer>> s = solutions.get(i);
                    buffer = new StringBuilder();
                    if ((logPopulation & LOG_GENOTYPE_MASK) == LOG_GENOTYPE_MASK) {
                        // Genotype
                        for (int j = 0; j < s.getVariables().size(); j++) {
                            buffer.append(s.getVariable(j).getValue()).append(";");
                        }
                        buffer.append(";");
                    }
                    if ((logPopulation & LOG_USED_GENES_MASK) == LOG_USED_GENES_MASK) {
                        // Used genes
                        buffer.append(problem.generatePhenotype(s).getUsedGenes()).append(";;");
                    }
                    if ((logPopulation & LOG_FITNESS_MASK) == LOG_FITNESS_MASK) {
                        // Fitness (many objectives)
                        for (int j = 0; j < s.getObjectives().size(); j++) {
                            buffer.append(s.getObjective(j));
                            buffer.append(";");
                        }
                        buffer.append(";");
                    }
                    if ((logPopulation & LOG_PHENOTYPE_MASK) == LOG_PHENOTYPE_MASK) {
                        // Phenotype
                        buffer.append(solStrings.get(i));
                        buffer.append(";;");
                    }
                    if ((logPopulation & LOG_EVALUATION_MASK) == LOG_EVALUATION_MASK) {
                        // Evaluation
                        ArrayList<double[]> timeTable = evaluator.getTimeTable();
                        for (int k = 0; k < timeTable.size(); ++k) {
                            double[] row = timeTable.get(k);
                            buffer.append(row[row.length - 1]).append(";");
                        }
                    }
                    buffer.append("\n");
                    addToLogFile(buffer.toString());
                }
                
            }
        }
    }

    @Override
    public void evaluate(Solution<Variable<Integer>> solution) {
        logger.severe("The solutions should be already evaluated. You should not see this message.");
    }

    @Override
    public void evaluate(Solution<Variable<Integer>> solution, Phenotype phenotype) {
        logger.severe("The solutions should be already evaluated. You should not see this message.");
    }

    @Override
    public GramEvalTemporalModel clone() {
        GramEvalTemporalModel clone = null;
        try {
            clone = new GramEvalTemporalModel(properties, threadId + 1, super.numberOfObjectives);
        } catch (IOException ex) {
            logger.severe(ex.getLocalizedMessage());
        }
        return clone;
    }

    public static Properties loadProperties(String propertiesFilePath) {
        Properties properties = new Properties();
        try {
            properties.load(new BufferedReader(new FileReader(new File(propertiesFilePath))));
            File clsDir = new File(properties.getProperty(com.engine.data.Common.WORK_DIR_PROP));
            URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class<URLClassLoader> sysclass = URLClassLoader.class;
            Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{clsDir.toURI().toURL()});
        } catch (Exception ex) {
            logger.severe(ex.getLocalizedMessage());
        }
        return properties;
    }
    
    public static void runGE(Properties properties, int threadId, Observer obs) {
        logger.setLevel(Level.ALL);

        // Log population:
        logPopulation = Integer.valueOf(properties.getProperty(com.engine.data.Common.LOG_POPULATION_PROP));
        if (logPopulation > 0) {
            gen = 0;
            String fileName = "Log_Population_" + currentDateTimeAsFormattedString() + ".csv";
            logPopulationOutputFile = properties.getProperty(com.engine.data.Common.LOGGER_BASE_PATH_PROP) + File.separator + fileName;
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
        if ((properties.getProperty(com.engine.data.Common.OBJECTIVES_PROP) != null)
                && (Integer.valueOf(properties.getProperty(com.engine.data.Common.OBJECTIVES_PROP)) == 2)) {
            numObjectives = 2;
        }
        problem = null;
        try {
            problem = new GramEvalTemporalModel(properties, threadId, numObjectives);
        } catch (IOException ex) {
            logger.severe(ex.getLocalizedMessage());
        }
        // Adjust some properties
        double crossOverProb = SinglePointCrossover.DEFAULT_PROBABILITY;
        if (properties.getProperty(com.engine.data.Common.CROSSOVER_PROB_PROP) != null) {
            crossOverProb = Double.valueOf(properties.getProperty(com.engine.data.Common.CROSSOVER_PROB_PROP));
        }
        double mutationProb = 1.0 / problem.reader.getRules().size();
        if (properties.getProperty(com.engine.data.Common.MUTATION_PROB_PROP) != null) {
            mutationProb = Double.valueOf(properties.getProperty(com.engine.data.Common.MUTATION_PROB_PROP));
        }
        
        int tournamentSize = 2;
        if (properties.getProperty(com.engine.data.Common.TOURNAMENT_SIZE_PROP) != null) {
            tournamentSize = Integer.valueOf(properties.getProperty(com.engine.data.Common.TOURNAMENT_SIZE_PROP));
        }
        
        // Set weight for CEG penalty
        UtilStats.setCEGPenalties(properties);
        
        // Second create the com.engine.algorithm
        IntegerFlipMutation<Variable<Integer>> mutationOperator = new IntegerFlipMutation<>(problem, mutationProb);
        SinglePointCrossover<Variable<Integer>> crossoverOperator = new SinglePointCrossover<>(problem, SinglePointCrossover.DEFAULT_FIXED_CROSSOVER_POINT, crossOverProb, SinglePointCrossover.AVOID_REPETITION_IN_FRONT);
        SimpleDominance<Variable<Integer>> comparator = new SimpleDominance<>();
        TournamentSelect<Variable<Integer>> selectionOp = new TournamentSelect<>(tournamentSize,comparator);
     
        if (numObjectives == 2) {
            algorithm = new ModifiedNSGAII(problem, Integer.valueOf(properties.getProperty(com.engine.data.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.data.Common.NUM_GENERATIONS_PROP)), mutationOperator, crossoverOperator, selectionOp);
        } else {
            algorithm = new SimpleGeneticAlgorithm<>(problem, Integer.valueOf(properties.getProperty(com.engine.data.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(com.engine.data.Common.NUM_GENERATIONS_PROP)), true, mutationOperator, crossoverOperator, selectionOp);
        }
        if (obs != null) {
            algorithm.addObserver(obs);
        }

        int numExecutions = 1;
        if (properties.getProperty(com.engine.data.Common.NUM_EXECUTIONS) != null) {
            numExecutions = Integer.valueOf(properties.getProperty(com.engine.data.Common.NUM_EXECUTIONS));
        }

        ArrayList<String> log = new ArrayList<>();

        int i=0;
        stop = false;
        while (!stop && (i < numExecutions)) {
            System.out.println("\nRun #" + i);
            System.out.println("========");

            problem.dataTable.resetBest();
            
            double startTime = System.currentTimeMillis();
            algorithm.initialize();
            Solutions<Variable<Integer>> solutions = algorithm.execute();
            double time = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("\nExecution time: " + time + " seconds.\n");

            executionReport.clear();

            if (numObjectives == 2) {
                if (i==0) {
                    solutionsTableModel.setColumnIdentifiers(new String[]{"RSME", "CEG", "Solution"});
                    solutionsTableModel.setRowCount(0);
                }
                for (Solution s : solutions) {
                    String[] solsForTable = new String[]{String.valueOf(s.getObjective(0)), String.valueOf(s.getObjective(1)),
                        problem.generatePhenotype(s).toString()};
                    solutionsTableModel.addRow(solsForTable);
                    executionReport.add(String.valueOf(s.getObjective(0)) + " " + String.valueOf(s.getObjective(1)) + ";" + problem.generatePhenotype(s).toString() + ";" + String.valueOf(time));
                }

            } else {
                if (i==0) {
                    solutionsTableModel.setColumnIdentifiers(new String[]{"Obj.", "Solution"});
                    solutionsTableModel.setRowCount(0);
                }
                String[] solsForTable = new String[]{String.valueOf(solutions.get(0).getObjective(0)),
                    problem.generatePhenotype(solutions.get(0)).toString()};
                solutionsTableModel.addRow(solsForTable);
                executionReport.add(String.valueOf(solutions.get(0).getObjective(0)) + ";" + problem.generatePhenotype(solutions.get(0)).toString() + ";" + String.valueOf(time));

                // Just for interrupted executions:
                System.out.println("\n\n@@;" + problem.generatePhenotype(solutions.get(0)).toString() + "\n\n");

            // Now we evaluate the solution in the validation com.engine.data
            logger.info("Validation of solutions[0] with fitness " + solutions.get(0).getObjective(0));
            problem.evaluate(solutions);
            Solution<Variable<Integer>> solution = solutions.get(0);
            // problem.evaluator.setTimeTable(problem.dataTable.getValidationTable());
            // double validationFitness = problem.dataTable.evaluate(problem.evaluator, solution, 0);
            /*if (Boolean.valueOf(properties.getProperty(com.engine.data.Common.VIEW_RESULTS_PROP))) {
                createDataPlot(problem.evaluator.getTimeTable(), "Predicted value (validation)", "yp (" + validationFitness + ")", problem.generatePhenotype(solution).toString());
            }*/
            // logger.info("Validation fitness for solutions[0] = " + validationFitness);
            logger.info("Average error for solutions[0]");
            logger.info("Training = " + problem.dataTable.computeAvgError(problem.dataTable.getTrainingTable()));
            logger.info("Validation = " + problem.dataTable.computeAvgError(problem.dataTable.getValidationTable()));

            }

            for (String s : GramEvalTemporalModel.executionReport) {
                log.add(i + ";" + s);
            }

             i++;
        }

        System.out.flush();
        System.err.flush();

        System.out.println("\n\n Execution report");
        System.out.println("==================\n");
        System.out.println("#Run;" + GramEvalTemporalModel.REPORT_HEADER);
        GramEvalTemporalModel.executionReport.clear();
        for (String s : log) {
            System.out.println(s);
            GramEvalTemporalModel.executionReport.add(s);
        }
        
    }

    
    private static DefaultTableModel solutionsTableModel = new DefaultTableModel();
    
    public static void setViewTable(JTable tableSolutions) {
        solutionsTableModel.setColumnIdentifiers(new String[]{"Solutions"});
        tableSolutions.setModel(solutionsTableModel);
    }

    public boolean validateAndDisplaySolutions(ArrayList<String> strSols) {
        if (validateSolutions(strSols)) {
            // Evaluate all the models
            for (int i = 0; i < strSols.size(); ++i) {
                evaluator.evaluateExpression(i);
//                createDataPlot(evaluator.getTimeTable(),"Evaluation", "Sol. "+i, strSols.get(i));
            }
            return true;
        } else {
            return false;
        }
                
    }
    
    /**
     * Returns a list of arrays with the evaluation of the models; the first array
     * is the reference (real) com.engine.data values.
     * 
     * @param strSols
     * @return 
     */
    public ArrayList<double[]> validateAndReturnEvaluations(ArrayList<String> strSols) {
        
        ArrayList<double[]> data = new ArrayList<>();
        
        if (validateSolutions(strSols)) {
            // Evaluate all the models.

            // First model is used to include real com.engine.data:
            evaluator.evaluateExpression(0);
            ArrayList<double[]> timeTable = evaluator.getTimeTable();                
            int numCols = timeTable.get(0).length; 
            
            double[] real = new double[timeTable.size()];
            double[] predicted = new double[timeTable.size()];
            

            for (int k = 0; k < timeTable.size(); ++k) {
                real[k] = timeTable.get(k)[0];
                predicted[k] = timeTable.get(k)[numCols-1];
            }
            
            data.add(real);
            data.add(predicted);
            
            /* Now continue evaluating the rest ot the models only taking predicted values. */
            for (int i = 1; i < strSols.size(); ++i) {
                evaluator.evaluateExpression(i);
                predicted = new double[timeTable.size()];
                for (int k = 0; k < timeTable.size(); ++k) {
                    predicted[k] = timeTable.get(k)[numCols-1];
                }
                data.add(predicted);
            }
                        
        }   
        
        return data;            
        
    }
    
    
    public ArrayList<String> validateAndReturnEvaluationsAsString(ArrayList<String> strSols, boolean includeInputs) {
        ArrayList<String> data = new ArrayList<>();
        
        if (validateSolutions(strSols)) {
            // Evaluate all the models.

            // First model is used to include real com.engine.data and input variables:
            evaluator.evaluateExpression(0);
            ArrayList<double[]> timeTable = evaluator.getTimeTable();                
            int numCols = timeTable.get(0).length; 
            
            String header = "# yr;";
            if (includeInputs) {
                for (int i = 1; i < numCols - 1; i++) {
                    header += "v" + (i - 1) + ";";
                }
            }

            for (int k = 0; k < timeTable.size(); ++k) {
                String line = "";
                double[] row = timeTable.get(k);
                // Real value
                line += row[0]+";";
                // Inputs
                if (includeInputs) {
                    for (int i = 1; i < numCols-1; i++) {
                        line += row[i] + ";";
                    }
                }
                data.add(line);
            }
            
            /* Now continue evaluating all the models (model 0 is evaluated again);
               only take predicted value. */
            for (int i = 0; i < strSols.size(); ++i) {
                header += strSols.get(i)+";";
                evaluator.evaluateExpression(i);
                for (int k = 0; k < timeTable.size(); ++k) {
                    double[] row = timeTable.get(k);
                    data.set(k, data.get(k)+row[numCols-1]+";");
                }
            }
            
            // Include header
            data.add(0, header);
            
        }   
        
        return data;
    }    
    
    
    public boolean validateSolutions(ArrayList<String> strSols) {
        if (generateAndCompileEvaluationFile(strSols)) {
            // And now we evaluate all the solutions with the compiled file:
            evaluator = null;
            try {
                evaluator = (AbstractPopEvaluator) (new MyLoader(compiler.getWorkDir())).loadClass("PopEvaluator" + threadId).newInstance();
                // Manage the VALIDATION table.
                evaluator.setTimeTable(dataTable.getValidationTable());
                if (this.properties.getProperty(com.engine.data.Common.REAL_DATA_COPIED_PROP) != null) {
                    evaluator.setRealDataCopied(Integer.valueOf(this.properties.getProperty(com.engine.data.Common.REAL_DATA_COPIED_PROP)));
                }
                if (this.properties.getProperty(com.engine.data.Common.MODEL_WIDTH_PROP) != null) {
                    evaluator.setModelWidth(Integer.valueOf(this.properties.getProperty(com.engine.data.Common.MODEL_WIDTH_PROP)));
                }
            } catch (Exception ex) {
                logger.severe(ex.getLocalizedMessage());
            }
            return true;
         } else {
            logger.severe("Could not compile evaluation file");
            return false;
        }   

    }
    
    
    public static void main(String[] args) {
        String propertiesFilePath = "TemporalModel.properties";
        int threadId = 1;
        if (args.length == 1) {
            propertiesFilePath = args[0];
        } else if (args.length >= 2) {
            propertiesFilePath = args[0];
            threadId = Integer.valueOf(args[1]);
        }
        Properties properties = loadProperties(propertiesFilePath);
        runGE(properties,threadId,null);
    }

    public static void stopExecution() {
        algorithm.stopExection();
        stop = true;
        logger.info("Execution stopped by user");
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
    

}
