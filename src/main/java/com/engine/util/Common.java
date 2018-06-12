package com.engine.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to mainly store constants.
 * 
 * @author J. M. Colmenar
 */
public class Common {
    public static final String NORMALIZED_DATA_PROP = "NormalizeData";
    public static final String TRAINING_PATH_PROP = "TrainingPath";
    public static final String VALIDATION_PATH_PROP = "ValidationPath";
    public static final String CROSSOVER_PROB_PROP = "CrossoverProb";
    public static final String MUTATION_PROB_PROP = "MutationProb";
    public static final String TOURNAMENT_SIZE_PROP = "TournamentSize";
    public static final String NUM_INDIVIDUALS_PROP = "NumIndividuals";
    public static final String NUM_GENERATIONS_PROP = "NumGenerations";
    public static final String LOGGER_BASE_PATH_PROP = "LoggerBasePath";
    public static final String LOGGER_LEVEL_PROP = "LoggerLevel";
    public static final String VIEW_RESULTS_PROP = "ViewResults";
    public static final String LOG_POPULATION_PROP = "LogPopulation";
    public static final String ERROR_THRESHOLD_PROP = "ErrorThreshold";
    public static final String BNF_PATH_FILE_PROP = "BnfPathFile";
    public static final String WORK_DIR_PROP = "WorkDir";
    public static final String CLASSPATH_SEPARATOR_PROP = "ClassPathSeparator";
    public static final String REAL_DATA_COPIED_PROP = "RealDataCopied";
    public static final String OBJECTIVES_PROP = "Objectives";
    public static final String NUM_EXECUTIONS = "Executions";
    public static final String MODEL_WIDTH_PROP = "ModelWidth";
    public static final String TEST_PATH_PROP = "TestPath";
    public static final String CHROMOSOME_LENGTH_PROP = "ChromosomeLength";
    public static final String MAX_WRAPS_PROP = "MaxWraps";
    public static final String SENSIBLE_INITIALIZATION = "SensibleInitialization";
    
    public static final int NUM_PROPS = 14;
    public static final String WINDOW_TITLE = "Pancreas Model Tools - ABSys Group (Complutense University of Madrid)";
    
    public static final Map<String,String> propsDescriptions = initializePropsDescriptions();
    public static final String CEG_PENALTY_B = "B-PenaltyCEG";
    public static final String CEG_PENALTY_C = "C-PenaltyCEG";
    public static final String CEG_PENALTY_D = "D-PenaltyCEG";
    public static final String CEG_PENALTY_E = "E-PenaltyCEG";
    
    
    
    private static Map<String, String> initializePropsDescriptions() {
        Map<String, String> m = new HashMap();
        m.put(BNF_PATH_FILE_PROP,"Grammar file ("+BNF_PATH_FILE_PROP+")");
        m.put(NUM_INDIVIDUALS_PROP,"Population value ("+NUM_INDIVIDUALS_PROP+")");
        m.put(NUM_GENERATIONS_PROP,"Generations value ("+NUM_GENERATIONS_PROP+")");
        m.put(CROSSOVER_PROB_PROP,"Crossover probability value ("+CROSSOVER_PROB_PROP+")");
        m.put(MUTATION_PROB_PROP,"Mutation probability value ("+MUTATION_PROB_PROP+")");    
        m.put(OBJECTIVES_PROP,"Objectives: 0 (RMSE), 1 (CEG), 2 (RMSE & CEG), 3 (R^2), 4 (Abs. Error) ("+OBJECTIVES_PROP+")");    
        m.put(NORMALIZED_DATA_PROP,"Normalize engine.data ("+NORMALIZED_DATA_PROP+")");
        m.put(TRAINING_PATH_PROP,"Training engine.data file ("+TRAINING_PATH_PROP+")");
        m.put(VALIDATION_PATH_PROP,"Validation engine.data file ("+VALIDATION_PATH_PROP+")");
        m.put(TEST_PATH_PROP,"Test engine.data file ("+TEST_PATH_PROP+")");
        m.put(LOGGER_BASE_PATH_PROP,"Logger directory ("+LOGGER_BASE_PATH_PROP+")");
        // m.put(LOGGER_LEVEL_PROP,"Logger level");  // Only works INFO level
        m.put(VIEW_RESULTS_PROP,"Graphically view results ("+VIEW_RESULTS_PROP+")");
        m.put(ERROR_THRESHOLD_PROP,"Error threshold value ("+ERROR_THRESHOLD_PROP+")");
        m.put(WORK_DIR_PROP,"Work directory ("+WORK_DIR_PROP+")");
        m.put(CLASSPATH_SEPARATOR_PROP,"ClassPath separator ("+CLASSPATH_SEPARATOR_PROP+")");
        m.put(REAL_DATA_COPIED_PROP,"Number of real engine.data copied in training evaluation ("+REAL_DATA_COPIED_PROP+")");
        m.put(MODEL_WIDTH_PROP,"Model width: number of engine.data to be taken into account for optimization, 0 means all engine.data ("+MODEL_WIDTH_PROP+")");
        m.put(LOG_POPULATION_PROP,"Log population: logs genotype, fitness, phenotype and evaluation of population individuals ("+LOG_POPULATION_PROP+")");
        m.put(MAX_WRAPS_PROP,"Maximum number of wraps ("+MAX_WRAPS_PROP+")");
        m.put(CHROMOSOME_LENGTH_PROP,"Number of codons, also called chromosome length ("+CHROMOSOME_LENGTH_PROP+")");
        m.put(TOURNAMENT_SIZE_PROP,"Number of individual to consider in tournament operator ("+TOURNAMENT_SIZE_PROP+")");
        return m;
    }

    public static String currentDateTimeAsFormattedString() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return sdfDate.format(new Date());
    }

}
