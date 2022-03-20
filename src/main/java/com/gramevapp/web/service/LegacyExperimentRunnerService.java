package com.gramevapp.web.service;

import com.engine.algorithm.ModelEvaluator;
import com.engine.algorithm.RunnableExpGramEv;
import com.engine.algorithm.SymbolicRegressionGE;
import com.gramevapp.web.model.*;
import com.gramevapp.web.repository.GrammarRepository;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.engine.util.Common.TRAINING_PATH_PROP;

@Service("legacyExperimentRunnerService")
public class LegacyExperimentRunnerService implements ExperimentRunner{

    private List<Thread> threads;
    private ExperimentService experimentService;
    private SaveDBService saveDBService;
    private Map<Long, Thread> threadMap;
    private Logger logger;
    private RunService runService;
    private Map<String, Long> threadRunMap;
    private Map<Long, RunnableExpGramEv> runnables;
    private boolean executionCancelled;

    // Constants
    private static final String RESOURCES = "resources";
    private static final String FILES = "files";
    private static final String GRAMMAR_DIR_PATH = "." + File.separator + RESOURCES + File.separator + FILES + File.separator + "grammar" + File.separator + "";
    private static final String DATATYPE_DIR_PATH = "." + File.separator + RESOURCES + File.separator + FILES + File.separator + "dataType" + File.separator + "";
    private static final String PROPERTIES_DIR_PATH = "." + File.separator + RESOURCES + File.separator + FILES + File.separator + "properties" + File.separator + "";
    private static final String WORK_DIR = "resources/files";
    private static final String CLASS_PATH_SEPARATOR = "\\;";
    private static final String CONFIGURATION = "configuration";
    private static final String INDEX = "index";
    private static final String EXPCONFIG = "expConfig";
    private static final String EXPDETAILS = "expDetails";
    private static final String RUNID = "runId";
    private static final String RUNLIST = "runList";
    private static final String RUN = "run";
    private static final String LISTYLINE = "listYLine";
    private static final String LISTFUNCTIONRESULT = "listFunctionResult";
    private static final String TRAININGRESULT = "trainingResult";
    private static final String TESTRESULT = "testResult";
    private static final String TESTLISTYLINE = "testListYLine";
    private static final String TESTLISTFUNCTIONRESULT = "testListFunctionResult";
    private static final String LOGGER_BASE_PATH = "resources/files/logs/population";

    // getter and setter
    public List<Thread> getThreads() {
        return threads;
    }

    public void setThreads(List<Thread> threads) {
        this.threads = threads;
    }

    public Map<Long, Thread> getThreadMap() {
        return threadMap;
    }

    public Map<Long, RunnableExpGramEv> getRunnables() {
        return runnables;
    }

    public void setThreadMap(Map<Long, Thread> threadMap) {
        this.threadMap = threadMap;
    }

    public Map<String, Long> getThreadRunMap() {
        return threadRunMap;
    }

    public void setThreadRunMap(Map<String, Long> threadRunMap) {
        this.threadRunMap = threadRunMap;
    }

    /*public Map<Long, RunnableExpGramEv> getRunnables() {
        return runnables;
    }*/

    public void setRunnables(Map<Long, RunnableExpGramEv> runnables) {
        this.runnables = runnables;
    }

    public LegacyExperimentRunnerService(ExperimentService experimentService, SaveDBService saveDBService
            , Map<Long, Thread> threadMap, RunService runService, Map<String, Long> threadRunMap
            , Map<Long, RunnableExpGramEv> runnables) {

        threads = new ArrayList<>();
        this.experimentService = experimentService;
        this.saveDBService = saveDBService;
        this.threadMap = threadMap;
        this.logger = Logger.getLogger(LegacyExperimentRunnerService.class.getName());
        this.runService = runService;
        this.threadRunMap = threadRunMap;
        this.runnables = runnables;
    }

    public void setExecutionCancelled(boolean newStatus) { this.executionCancelled = newStatus; }

    @Override
    public void accept(Run run, String propPath, int crossRunIdentifier, String objective, boolean de) {
        try {
            System.out.println("ACEPTOOO");
            threads.add(runExperimentDetails(run, propPath, crossRunIdentifier, objective, de));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startExperiment() {
        // Use half of the available processors.
        System.out.println("EMPIEZO 1");
        int availableProcessors = Runtime.getRuntime().availableProcessors() / 2;

        Thread thread = new Thread(() -> {
            try {
                int i = 0;
                while (i < threads.size() && !executionCancelled) {
                    int limit = availableProcessors;
                    if ((threads.size()-i) < availableProcessors) limit = threads.size()-i;
                    // Start threads
                    for (int j = i; j < i+limit; j++) {
                        System.out.println("HE LANZADO UNO");
                        threads.get(j).start();
                    }
                    // Wait for them
                    for (int j = i; j < i+limit; j++) {
                        threads.get(j).join();
                    }
                    i += limit;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.println("LANZO CREADOR");
        thread.start();
    }

    public Thread runExperimentDetails(Run run, String propPath, int crossRunIdentifier, String objective, boolean de) throws IOException {

        File propertiesFile = new File(propPath);
        Properties properties = new Properties();

        // Try-with-resources does not need closing stream
        try(Reader propertiesReader = new FileReader(propertiesFile)) {
            properties.load(propertiesReader);
        }
        properties.setProperty(TRAINING_PATH_PROP, propPath);


        RunnableExpGramEv obj = new RunnableExpGramEv(properties, run,
                experimentService.findExperimentDataTypeById(run.getExperimentId().getDefaultExpDataType()), runService,
                saveDBService, crossRunIdentifier, objective, de);
        Thread.UncaughtExceptionHandler h = (th, ex) -> {
            run.setStatus(Run.Status.FAILED);
            run.setExecReport(run.getExecReport() + "\nUncaught exception: " + ex);
            String warningMsg = "Uncaught exception: " + ex;
            logger.warning(warningMsg);
        };
        Thread th = new Thread(obj);
        th.setUncaughtExceptionHandler(h);
        threadMap.put(th.getId(), th);
        threadRunMap.put(th.getName(), run.getId());
        run.setThreaId(th.getId());
        runnables.put(th.getId(), obj);
        // https://stackoverflow.com/questions/26213615/terminating-thread-using-thread-id-in-java

        return th;
    }

    public HashMap<String,List<Double>> collectTrainingAndTestStats(Run run, boolean mustConsiderCrossValidation) {

        boolean considerCrossValidation = mustConsiderCrossValidation && run.getExperimentId().isCrossExperiment();

        HashMap<String, List<Double>> results = new HashMap<>();

        int crossRunIdentifier = run.getExperimentId().getIdRunList().indexOf(run) + 1;
        List<Double> listYLine = new ArrayList<>();
        List<Double> listFunctionResult = new ArrayList<>();
        List<Double> trainingResult = new ArrayList<>();
        String[] splitContent =
                experimentService.findExperimentDataTypeById(run.getExperimentId().getDefaultExpDataType()).getInfo().split("\r\n");
        String[] testSplitContent = null;

        if (considerCrossValidation) {
            List<String> splitContentList = new ArrayList<>();
            List<String> testSplitContentList = new ArrayList<>();
            splitContent[0] = splitContent[0].substring(0, splitContent[0].length() - 5) + "\r\n";
            splitContentList.add(splitContent[0]);
            testSplitContentList.add(splitContent[0]);

            int indexFold;
            for (int i = 1; i < splitContent.length; i++) {
                indexFold = splitContent[i].lastIndexOf(';');
                if (Integer.parseInt(splitContent[i].substring(indexFold + 1)) != crossRunIdentifier) {
                    splitContentList.add(splitContent[i].substring(0, indexFold) + "\r\n");
                } else {
                    testSplitContentList.add(splitContent[i].substring(0, indexFold) + "\r\n");
                }
            }
            splitContent = splitContentList.toArray(new String[0]);
            testSplitContent = testSplitContentList.toArray(new String[0]);
        }

        processExperimentDataTypeInfo(splitContent, listYLine, listFunctionResult, trainingResult, run);


        results.put(LISTYLINE,listYLine);
        results.put(LISTFUNCTIONRESULT,listFunctionResult);
        results.put(TRAININGRESULT,trainingResult);


        if (run.getExperimentId().getDefaultTestExpDataTypeId() != null || considerCrossValidation) {
            List<Double> testListYLine = new ArrayList<>();
            List<Double> testListFunctionResult = new ArrayList<>();
            List<Double> testResult = new ArrayList<>();
            if (considerCrossValidation) {
                splitContent = testSplitContent;
            } else {
                splitContent = experimentService.findExperimentDataTypeById(run.getExperimentId().getDefaultTestExpDataTypeId()).getInfo().split("\r\n");
            }
            if (splitContent != null) {
                processExperimentDataTypeInfo(splitContent, testListYLine, testListFunctionResult, testResult, run);
                results.put(TESTLISTYLINE,testListYLine);
                results.put(TESTLISTFUNCTIONRESULT,testListFunctionResult);
                results.put(TESTRESULT,testResult);
            }
        }

        return results;
    }

    public static void processExperimentDataTypeInfo(String[] splitContent, List<Double> listYLine, List<Double> listFunctionResult, List<Double> result,
                                                     Run run) {
        double[] yDoubleArray = new double[splitContent.length - 1];
        double[] functionResultDoubleArray = new double[splitContent.length - 1];
        double yValue;
        double modelValue = -1;

        for (int i = 1; i < splitContent.length; i++) {
            String[] contentSplit = splitContent[i].split(";");
            yValue = Double.parseDouble(contentSplit[0]);
            if (run.getModel() != null && !run.getModel().isEmpty()) {
                modelValue = SymbolicRegressionGE.calculateFunctionValuedResultWithCSVData(run.getModel(),
                        contentSplit);
                if (listFunctionResult != null)
                    listFunctionResult.add(modelValue);
            }
            if (listYLine != null) {
                listYLine.add(yValue);
            }

            yDoubleArray[i - 1] = yValue;
            if (modelValue != -1)
                functionResultDoubleArray[i - 1] = modelValue;
        }
        // RMSE AVGERROR RSQUARE ABSOLUTEERROR
        result.add(ModelEvaluator.computeRMSE(yDoubleArray, functionResultDoubleArray));
        result.add(ModelEvaluator.computeAvgError(yDoubleArray, functionResultDoubleArray));
        result.add(ModelEvaluator.computeR2(yDoubleArray, functionResultDoubleArray));
        result.add(ModelEvaluator.computeAbsoluteError(yDoubleArray, functionResultDoubleArray));
        result.add(ModelEvaluator.computeRelativeError(yDoubleArray, functionResultDoubleArray));
    }

    public void modelAddData(Model model, User user, Dataset experimentDataType,
                             List<Dataset> experimentDataTypeList, Long testExperimentDataTypeId, GrammarRepository grammarRepository) {
        Dataset testExperimentDataType = testExperimentDataTypeId == null ? null : experimentService.findDataTypeById(testExperimentDataTypeId);
        model.addAttribute("grammarList", grammarRepository.findByUserId(user.getId()));
        model.addAttribute("datasetList", experimentService.findAllExperimentDataTypeByUserId(user.getId()));
        model.addAttribute("experimentDataType", experimentDataType);
        model.addAttribute("testExperimentDataType", testExperimentDataType);
        model.addAttribute("dataTypeList", experimentDataTypeList);
    }

    public void createPropertiesFile(String propertiesFilePath, String expName,
                                      ConfigExperimentDto configExpDto, User user, String grammarFilePath,
                                      String dataTypeDirectoryPath, Dataset expDataType) throws IOException {
        File propertiesNewFile = new File(propertiesFilePath);
        if ((!propertiesNewFile.exists()) && (!propertiesNewFile.createNewFile())) {
            String logMsg = "Cannot create properties file at "+propertiesFilePath;
            logger.log(Level.SEVERE, logMsg);
        }
        PrintWriter propertiesWriter = new PrintWriter(propertiesNewFile);

        propertiesWriter.println("# ExpPropertiesDto for " + expName);
        propertiesWriter.println("LoggerBasePath=" + (LOGGER_BASE_PATH + File.separator + user.getId()).replace("\\", "/"));
        propertiesWriter.println("ErrorThreshold=" + 0.0);
        propertiesWriter.println("TournamentSize=" + configExpDto.getTournament());
        propertiesWriter.println("WorkDir=" + WORK_DIR.replace("\\", "/"));
        propertiesWriter.println("RealDataCopied=" + 0);
        propertiesWriter.println("CrossoverProb=" + configExpDto.getCrossoverProb());
        propertiesWriter.println("BnfPathFile=" + grammarFilePath.substring(2).replace("\\", "/"));
        propertiesWriter.println("Objectives=" + 0);
        propertiesWriter.println("ClassPathSeparator=" + CLASS_PATH_SEPARATOR);
        propertiesWriter.println("Executions=" + 1);
        propertiesWriter.println("LoggerLevel=");
        propertiesWriter.println("MutationProb=" + configExpDto.getMutationProb());
        propertiesWriter.println("NormalizeData=" + false);
        propertiesWriter.println("LogPopulation=" + 1);
        propertiesWriter.println("ChromosomeLength=" + configExpDto.getNumCodons());
        propertiesWriter.println("NumIndividuals=" + configExpDto.getPopulationSize());
        propertiesWriter.println("NumGenerations=" + configExpDto.getGenerations());
        propertiesWriter.println("ViewResults=" + false);
        propertiesWriter.println("MaxWraps=" + configExpDto.getMaxWraps());
        propertiesWriter.println("ModelWidth=" + 500);
        propertiesWriter.println("TrainingPath=" + (dataTypeDirectoryPath + File.separator + configExpDto.getExperimentName()
                + "_" + expDataType.getId() + ".csv").substring(2).replace("\\", "/"));
        propertiesWriter.println("isDE=" + configExpDto.isDe());
        propertiesWriter.println("LowerBoundDE=" + configExpDto.getLowerBoundDE());
        propertiesWriter.println("UpperBoundDE=" + configExpDto.getUpperBoundDE());
        propertiesWriter.println("RecombinationFactorDE=" + configExpDto.getRecombinationFactorDE());
        propertiesWriter.println("MutationFactorDE=" + configExpDto.getMutationFactorDE());
        propertiesWriter.println("PopulationSizeDE=" + configExpDto.getPopulationDE());
        propertiesWriter.close();
    }

    public void initSystemStream() {
        String messageSkip = "\u001B[0;39m \u001B[36mc.engine.algorithm.SymbolicRegressionGE \u001B[0;39m \u001B[2m:\u001B[0;39m ";
        System.setOut(new PrintStream(System.out) {
            @Override
            public void write(byte[] buf, int off, int len) {
                //Convert byte[] to String
                String logInfo = new String(buf);
                if (logInfo.contains("j.c.algorithm.ga.SimpleGeneticAlgorithm") ||
                        logInfo.contains("c.engine.algorithm.SymbolicRegressionGE")) {
                    String infoFormatted = "";
                    Pattern pattern =
                            Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}");
                    Matcher matcher = pattern.matcher(logInfo);
                    if (matcher.find()) {
                        infoFormatted += matcher.group() + (logInfo.contains("j.c.algorithm.ga.SimpleGeneticAlgorithm") ?
                                " j.c.algorithm.ga.SimpleGeneticAlgorithm :" : " c.engine.algorithm.SymbolicRegressionGE :");
                    }
                    pattern =
                            Pattern.compile("[0-9]{1,3}% performed.*$");
                    matcher = pattern.matcher(logInfo);

                    if (matcher.find()) {
                        //j.c.algorithm.ga.SimpleGeneticAlgorithm
                        infoFormatted += matcher.group() + "\r\n";
                    } else {
                        //c.engine.algorithm.SymbolicRegressionGE
                        infoFormatted += logInfo.substring(logInfo.indexOf(messageSkip) + messageSkip.length());
                    }
                    pattern =
                            Pattern.compile("Thread-[0-9]+");
                    matcher = pattern.matcher(logInfo);
                    if (matcher.find()) {
                        String threadName = matcher.group();

                        if (infoFormatted.contains("2m---\u001B[0;39m \u001B[2m[     Thread-")) {
                            infoFormatted = infoFormatted.replaceAll("2m---\u001B\\[0;39m \u001B\\[2m\\[     Thread-[0-9]+]\u001B\\[0;39m \u001B\\[36mj.c.algorithm.ga.SimpleGeneticAlgorithm \u001B\\[0;39m \u001B\\[2m:\u001B\\[0;39m ", "");
                        }

                        runService.updateExecutionReport(threadRunMap.get(threadName),infoFormatted);

                    }
                }
                super.write(buf, off, len);
            }
        });
    }

    public void removeRuns(Experiment exp) {
        List<Run> oldRunList = new ArrayList<>(exp.getIdRunList());
        //remove old run
        for (Run oldRun : oldRunList) {
            Thread th = threadMap.get(oldRun.getThreaId());
            if (th != null && th.isAlive()) {
                th.interrupt();
                runnables.get(oldRun.getThreaId()).stopExecution();
            }
            exp.removeRun(oldRun);
            runService.deleteRun(oldRun);
        }
    }

    public void runSection(Run run, Experiment exp) {
        run.setStatus(Run.Status.INITIALIZING);
        run.setIniDate(new Timestamp(new Date().getTime()));
        run.setModificationDate(new Timestamp(new Date().getTime()));
        exp.getIdRunList().add(run);
        run.setExperimentId(exp);
        run.setExecReport(run.getExecReport()+"Initializing...\n\n");
    }

    public String grammarFileSection(User user, ConfigExperimentDto configExpDto, String grammar, GrammarRepository grammarRepository) throws IOException {

        File dir = new File(GRAMMAR_DIR_PATH + user.getId());
        if (!dir.exists())
            dir.mkdirs();

        String grammarFilePath = GRAMMAR_DIR_PATH + user.getId() + File.separator + configExpDto.getExperimentName().replaceAll("\\s+", "") + "_" +
                grammarRepository.getNextValue() + ".bnf";

        File grammarNewFile = new File(grammarFilePath);
        if((!grammarNewFile.exists()) && (!grammarNewFile.createNewFile())) {
            logger.log(Level.SEVERE, "Grammar file could not be created at {0}",grammarFilePath);
        }

        PrintWriter grammarWriter = new PrintWriter(grammarNewFile);

        String[] parts = grammar.split("\\r\\n");
        for (String part : parts) {
            grammarWriter.println(part);
        }

        grammarWriter.close();

        return grammarFilePath;
    }

    public Experiment experimentSection(Experiment exp, User user, Dataset testExpDataType,
                                         Dataset expDataType,
                                         ConfigExperimentDto configExpDto, String grammar, boolean removeRuns) {
        if (exp == null) {   // We create it
            exp = new Experiment(user, configExpDto.getExperimentName(), configExpDto.getExperimentDescription(), configExpDto.getGenerations(),
                    configExpDto.getPopulationSize(), configExpDto.getMaxWraps(), configExpDto.getTournament(), configExpDto.getCrossoverProb(), configExpDto.getMutationProb(),
                    configExpDto.getNumCodons(), configExpDto.getNumberRuns(), configExpDto.getObjective(),
                    new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime()), configExpDto.isDe(),
                    configExpDto.getLowerBoundDE(), configExpDto.getUpperBoundDE(), configExpDto.getRecombinationFactorDE(), configExpDto.getMutationFactorDE(), configExpDto.getTagsText(), configExpDto.getPopulationDE());

        } else {  // The experiment data type configuration already exist
            exp.setUserId(user);

            exp.setExperimentName(configExpDto.getExperimentName());
            exp.setExperimentDescription(configExpDto.getExperimentDescription());
            exp.setGenerations(configExpDto.getGenerations());
            exp.setPopulationSize(configExpDto.getPopulationSize());
            exp.setMaxWraps(configExpDto.getMaxWraps());
            exp.setTournament(configExpDto.getTournament());
            exp.setCrossoverProb(configExpDto.getCrossoverProb());
            exp.setMutationProb(configExpDto.getMutationProb());
            exp.setNumCodons(configExpDto.getNumCodons());
            exp.setNumberRuns(configExpDto.getNumberRuns());
            exp.setObjective(configExpDto.getObjective());
            exp.setDe(configExpDto.isDe());
            exp.setCreationDate(new Timestamp(new Date().getTime()));
            exp.setModificationDate(new Timestamp(new Date().getTime()));
            exp.setLowerBoundDE(configExpDto.getLowerBoundDE());
            exp.setUpperBoundDE(configExpDto.getUpperBoundDE());
            exp.setRecombinationFactorDE(configExpDto.getRecombinationFactorDE());
            exp.setMutationFactorDE(configExpDto.getMutationFactorDE());
            exp.setTags(configExpDto.getTagsText());
            exp.setPopulationDE(configExpDto.getPopulationDE());
            if (removeRuns)
                removeRuns(exp);

        }
        exp.addExperimentDataType(expDataType);
        exp.setDefaultGrammar(grammar);
        exp.setDefaultExpDataType(expDataType.getId());
        exp.setDefaultTestExpDataTypeId(testExpDataType != null ? testExpDataType.getId() : null);
        exp.setModificationDate(new Timestamp(new Date().getTime()));
        exp.setCrossExperiment(configExpDto.getCrossExperiment().equals("true"));
        return exp;
    }

    public ConfigExperimentDto fillConfigExpDto(ConfigExperimentDto configExpDto, Experiment exp, String grammar,
                                                Dataset dataset, boolean forEqual) {

        setConfigExpDtoWIthExperiment(configExpDto, forEqual ? configExpDto.getExperimentName() : exp.getExperimentName(),
                forEqual ? configExpDto.getExperimentDescription() : exp.getExperimentDescription(), exp.getCrossoverProb(), exp.getGenerations(),
                exp.getPopulationSize(), exp.getMaxWraps(), exp.getTournament(), exp.getMutationProb(),
                exp.getNumCodons(), exp.getNumberRuns(), exp.getObjective(), exp.isDe(), exp.getLowerBoundDE(), exp.getUpperBoundDE(), exp.getRecombinationFactorDE(), exp.getMutationFactorDE(),
                forEqual ? configExpDto.getTagsText() : exp.getTags(), exp.getPopulationDE());

        configExpDto.setId(exp.getId());
        configExpDto.setDefaultExpDataTypeId(exp.getDefaultExpDataType());
        if (!forEqual) {
            configExpDto.setFileText(grammar);
        }
        configExpDto.setCrossExperiment(exp.isCrossExperiment() ? "true" : "false");
        configExpDto.setContentFold(dataset.getInfo().contains("K-Fold"));

        return configExpDto;
    }

    public void setConfigExpDtoWIthExperiment(ConfigExperimentDto configExpDto, String experimentName, String experimentDescription,
                                               Double crossoverProb, Integer generations, Integer populationSize, Integer maxWraps,
                                               Integer tournament, Double mutationProb,
                                               Integer numCodons, Integer numberRuns, String objective, boolean de,
                                               Double lowerBoundDE, Double upperBoundDE, Double recombinationFactorDE, Double mutationFactorDE,
                                               String tags, Integer populationDE) {
        configExpDto.setExperimentName(experimentName);
        configExpDto.setExperimentDescription(experimentDescription);
        configExpDto.setCrossoverProb(crossoverProb);
        configExpDto.setGenerations(generations);
        configExpDto.setPopulationSize(populationSize);
        configExpDto.setMaxWraps(maxWraps);
        configExpDto.setTournament(tournament);
        configExpDto.setCrossoverProb(crossoverProb);
        configExpDto.setMutationProb(mutationProb);
        configExpDto.setNumCodons(numCodons);
        configExpDto.setNumberRuns(numberRuns);
        configExpDto.setObjective(objective);
        configExpDto.setDe(de);
        configExpDto.setLowerBoundDE(lowerBoundDE);
        configExpDto.setUpperBoundDE(upperBoundDE);
        configExpDto.setRecombinationFactorDE(recombinationFactorDE);
        configExpDto.setMutationFactorDE(mutationFactorDE);
        configExpDto.setTagsText(tags);
        configExpDto.setPopulationDE(populationDE);
    }
}
