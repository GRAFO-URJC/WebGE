package com.gramevapp.web.service;

import com.engine.algorithm.CallableExpGramEv;
import com.gramevapp.web.model.*;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import com.engine.algorithm.ModelEvaluator;
import com.engine.algorithm.SymbolicRegressionGE;
import com.gramevapp.web.repository.GrammarRepository;
import java.io.*;
import java.sql.Timestamp;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.engine.util.Common.TRAINING_PATH_PROP;

@Service("threadPoolExperimentRunnerService")
public class ThreadPoolExperimentRunnerService implements ExperimentRunner{

    private ExperimentService experimentService;
    private SaveDBService saveDBService;
    private GrammarRepository grammarRepository;
    private UserService userService;
    private Logger logger;
    private RunService runService;
    private boolean executionCancelled;
    private Map<Long, Future<Void>> runToFuture;
    private Map<Long, CallableExpGramEv> runToCallable;
    private Run[] runElementsInExecution;

    private ExecutorService threadPool;



    public ThreadPoolExperimentRunnerService(ExperimentService experimentService, SaveDBService saveDBService
            , RunService runService, Map<Long, Future<Void>> runToFuture
            , Map<Long, CallableExpGramEv> runToCallable, GrammarRepository grammarRepository, UserService userService) {
        this.experimentService = experimentService;
        this.saveDBService = saveDBService;
        this.logger = Logger.getLogger(ThreadPoolExperimentRunnerService.class.getName());
        this.runService = runService;
        this.runToFuture = runToFuture;
        this.runToCallable = runToCallable;

        this.grammarRepository = grammarRepository;
        this.userService = userService;

        int numThreads = Runtime.getRuntime().availableProcessors()/2;
        this.threadPool = Executors.newFixedThreadPool(numThreads);
    }

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
    private static final String CONFIGEXPERIMENTPATH = "experiment/configExperiment";


    public void setExecutionCancelled(boolean newStatus) { this.executionCancelled = newStatus; }

    @Override
    public Future<Void> accept(Run run, String propPath, int crossRunIdentifier, String objective, boolean de, Long expId) {
        try {
            return threadPool.submit(runExperimentDetailsServiceWorker(run, propPath, crossRunIdentifier, objective, de));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void accept(List<Thread> threadList, Run run, String propPath, int crossRunIdentifier, String objective, boolean de) {

    }

    public String runExperimentService(Model model, String experimentDataTypeId, String testExperimentDataTypeId
            , FileModelDto fileModelDto, ConfigExperimentDto configExpDto, BindingResult result
            , RedirectAttributes redirectAttrs) throws IOException {

        User user = userService.getLoggedInUser();
        modelAddDataService(model, user, null, null, null);

        // Check the data received
        if (result.hasErrors()) {
            model.addAttribute(CONFIGURATION, configExpDto);
            return CONFIGEXPERIMENTPATH;
        }


        // Experiment Data Type SECTION
        Dataset expDataType = experimentService.
                findExperimentDataTypeById(Long.valueOf(experimentDataTypeId));

        experimentDataTypeSection(expDataType);
        // END - Experiment Data Type SECTION

        // Experiment section:
        Experiment exp = experimentSectionService(configExpDto.getId() != null ?
                        experimentService.findExperimentById(configExpDto.getId()) : null
                , user,
                (testExperimentDataTypeId.equals("")) ? null : experimentService.
                        findExperimentDataTypeById(Long.valueOf(testExperimentDataTypeId))
                , expDataType, configExpDto, configExpDto.getFileText(), true);
        experimentService.saveExperiment(exp);

        long expId = exp.getId();
        // END - Experiment section

        // Grammar File SECTION
        String grammarFilePath = grammarFileSectionService(user, configExpDto, exp.getDefaultGrammar());
        // END - Grammar File SECTION

        Run run = null;
        String propPath;

        runElementsInExecution = new Run[configExpDto.getNumberRuns()];
        List<Future<Void>> futures = new ArrayList<>();

        //check if need to run more runs
        for (int i = 0; i < configExpDto.getNumberRuns(); i++) {
            // RUN SECTION
            run = runService.saveRun(new Run());
            Long runId = run.getId();
            runSectionService(run, exp);
            run.setStatus(Run.Status.WAITING);
            // Create ExpPropertiesDto file
            propPath = expPropertiesSet(configExpDto, user, expDataType, grammarFilePath);
            // Run experiment in new thread
            int crossRunIdentifier = exp.isCrossExperiment() ? run.getExperimentId().getIdRunList().indexOf(run) + 1 : -1;
            Future<Void> future = accept(run, propPath, crossRunIdentifier, configExpDto.getObjective(), configExpDto.isDe(), expId);
            runToFuture.put(runId, future);
            futures.add(future);
            runElementsInExecution[i] = run;
        }
        experimentService.saveExperiment(exp);
        executionCancelled = false;

        // Start all runs and handle their exceptions.
        threadPool.submit(() ->  {
            int tareasFinalizadas = 0;
            for (Future<Void> resultFuture : futures) {
                try {
                    resultFuture.get();
                } catch (InterruptedException e) {
                    logger.warning("Interrupted thread in service worker");
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Run runFinish = runElementsInExecution[tareasFinalizadas];
                    runFinish.setStatus(Run.Status.FAILED);
                    runFinish.setExecReport(runFinish.getExecReport() + "\nUncaught exception: " + e);
                    String warningMsg = "Uncaught exception: " + e;
                    logger.warning(warningMsg);
                } finally {
                    tareasFinalizadas++;
                }
            }
        });

        redirectAttrs.addAttribute("id", exp.getId());
        redirectAttrs.addAttribute("loadExperimentButton", "loadExperimentButton");
        return "redirect:/experiment/expRepoSelected";
    }

    public CallableExpGramEv runExperimentDetailsServiceWorker(Run run, String propPath, int crossRunIdentifier, String objective, boolean de) throws IOException {

        File propertiesFile = new File(propPath);
        Properties properties = new Properties();

        // Try-with-resources does not need closing stream
        try(Reader propertiesReader = new FileReader(propertiesFile)) {
            properties.load(propertiesReader);
        }
        properties.setProperty(TRAINING_PATH_PROP, propPath);


        CallableExpGramEv obj = new CallableExpGramEv(properties, run,
                experimentService.findExperimentDataTypeById(run.getExperimentId().getDefaultExpDataType()), runService,
                saveDBService, crossRunIdentifier, objective, de);

        runToCallable.put(run.getId(), obj);
        return obj;
    }

    public Map<String,List<Double>> collectTrainingAndTestStats(Run run, boolean mustConsiderCrossValidation) {

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

    public void modelAddDataService(Model model, User user, Dataset experimentDataType,
                                    List<Dataset> experimentDataTypeList, Long testExperimentDataTypeId) {
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
//                    pattern =
//                            Pattern.compile("Thread-[0-9]+");
//                    matcher = pattern.matcher(logInfo);
//                    if (matcher.find()) {
//                        String threadName = matcher.group();
//                        if (infoFormatted.contains("2m---\u001B[0;39m \u001B[2m[     Thread-")) {
//                            infoFormatted = infoFormatted.replaceAll("2m---\u001B\\[0;39m \u001B\\[2m\\[     Thread-[0-9]+]\u001B\\[0;39m \u001B\\[36mj.c.algorithm.ga.SimpleGeneticAlgorithm \u001B\\[0;39m \u001B\\[2m:\u001B\\[0;39m ", "");
//                        }
//                        //runService.updateExecutionReport(threadRunMap.get(threadName),infoFormatted);
//                    }
                }
                super.write(buf, off, len);
            }
        });
    }

    public void removeRunsService(Experiment exp) {
        List<Run> oldRunList = new ArrayList<>(exp.getIdRunList());
        //remove old run
        for (Run oldRun : oldRunList) {
            exp.removeRun(oldRun);
            runService.deleteRun(oldRun);
        }
    }

    public void runSectionService(Run run, Experiment exp) {
        run.setStatus(Run.Status.INITIALIZING);
        run.setIniDate(new Timestamp(new Date().getTime()));
        run.setModificationDate(new Timestamp(new Date().getTime()));
        exp.getIdRunList().add(run);
        run.setExperimentId(exp);
        run.setExecReport(run.getExecReport()+"Initializing...\n\n");
    }

    public String grammarFileSectionService(User user, ConfigExperimentDto configExpDto, String grammar) throws IOException {

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

    public Experiment experimentSectionService(Experiment exp, User user, Dataset testExpDataType,
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
                removeRunsService(exp);

        }
        exp.addExperimentDataType(expDataType);
        exp.setDefaultGrammar(grammar);
        exp.setDefaultExpDataType(expDataType.getId());
        exp.setDefaultTestExpDataTypeId(testExpDataType != null ? testExpDataType.getId() : null);
        exp.setModificationDate(new Timestamp(new Date().getTime()));
        exp.setCrossExperiment(configExpDto.getCrossExperiment().equals("true"));
        return exp;
    }

    public ConfigExperimentDto fillConfigExpDtoService(ConfigExperimentDto configExpDto, Experiment exp, String grammar,
                                                       Dataset dataset, boolean forEqual) {

        setConfigExpDtoWIthExperimentService(configExpDto, forEqual ? configExpDto.getExperimentName() : exp.getExperimentName(),
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

    public void setConfigExpDtoWIthExperimentService(ConfigExperimentDto configExpDto, String experimentName, String experimentDescription,
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

    public String stopRunExperimentService(Model model, String runIdStop, RedirectAttributes redirectAttrs
            , DiagramDataService diagramDataService) throws InterruptedException {
        Run run = runService.findByRunId(Long.parseLong(runIdStop));
        Long runId = run.getId();
        runToCallable.get(runId).stopExecution();
        run.getDiagramData().setStopped(true);
        diagramDataService.saveDiagram(run.getDiagramData());
        run.setStatus(Run.Status.STOPPED);
        runService.saveRun(run);

        if (model != null) {
            model.addAttribute(EXPDETAILS, run.getExperimentId());
            model.addAttribute(RUNID, run.getId());
            model.addAttribute(RUN, run);
            model.addAttribute(INDEX, run.getExperimentId().getIdRunList().indexOf(run) + 1);
        }

        return "experiment/experimentDetails";
    }

    public Long expRepoSelectedDeleteService(String experimentId) {

        Long idExp = Long.parseLong(experimentId);
        Experiment expConfig = experimentService.findExperimentById(idExp);

        Iterator<Run> listRunIt = expConfig.getIdRunList().iterator();
        while (listRunIt.hasNext()) {
            Run runIt = listRunIt.next();
            if(runIt != null) {
                runIt.setStatus(Run.Status.STOPPED);
                runService.saveRun(runIt);

                Long runId = runIt.getId();
                Future<Void> runFuture = runToFuture.get(runId);
                if(runFuture != null) {
                    // No se interrumpen los hilos del threadpool
                    // Solo detenemos el run.
                    runToCallable.get(runId).stopExecution();
                }
                listRunIt.remove();
                runIt.setExperimentId(null);
                runIt.setStatus(Run.Status.STOPPED);
                runService.saveRun(runIt);
            }
        }

        Iterator<Dataset> listDataTypeIt = expConfig.getIdExpDataTypeList().iterator();
        while (listDataTypeIt.hasNext()) {
            listDataTypeIt.next();
            listDataTypeIt.remove();
        }

        experimentService.saveExperiment(expConfig);
        experimentService.deleteExperiment(expConfig);
        return idExp;
    }

    public void experimentDataTypeSection(Dataset expDataType) {
        expDataType.setCreationDate(new Timestamp(new Date().getTime()));
        expDataType.setDataTypeType("training");
    }

    public String saveExperimentService(Model model, String experimentDataTypeId, String testExperimentDataTypeId
            , FileModelDto fileModelDto, ConfigExperimentDto configExpDto, BindingResult result) {

        User user = userService.getLoggedInUser();
        this.modelAddDataService(model, user, null, null, null);
        model.addAttribute(CONFIGURATION, configExpDto);

        if (result.hasErrors()) {
            return CONFIGEXPERIMENTPATH;
        }

        Experiment exp = null;

        // Experiment Data Type SECTION
        Dataset expDataType;
        if (experimentDataTypeId.equals("-1")) {
            model.addAttribute(CONFIGURATION, configExpDto);
            model.addAttribute(EXPCONFIG, configExpDto);
            result.rejectValue("typeFile", "error.typeFile", "Choose one file");
            return CONFIGEXPERIMENTPATH;
        } else {
            expDataType = experimentService.findDataTypeById(Long.parseLong(experimentDataTypeId));
        }

        experimentDataTypeSection(expDataType);
        // END - Experiment Data Type SECTION

        // Experiment section:
        Dataset testExperimentDataType = (testExperimentDataTypeId.equals("")) ? null : experimentService.
                findExperimentDataTypeById(Long.valueOf(testExperimentDataTypeId));


        boolean sameExp = false;
        if (configExpDto.getId() != null) {
            exp = experimentService.findExperimentById(configExpDto.getId());

            /*Check if exp only changed name, desc or tags, in that case, dont remove runs*/
            sameExp =
                    exp.getGenerations().equals(configExpDto.getGenerations()) &&
                            exp.getCrossoverProb().equals(configExpDto.getCrossoverProb()) &&
                            exp.getPopulationSize().equals(configExpDto.getPopulationSize()) &&
                            exp.getMutationProb().equals(configExpDto.getMutationProb()) &&
                            exp.getMaxWraps().equals(configExpDto.getMaxWraps()) &&
                            exp.getNumCodons().equals(configExpDto.getNumCodons()) &&
                            exp.getTournament().equals(configExpDto.getTournament()) &&
                            exp.getNumberRuns().equals(configExpDto.getNumberRuns()) &&
                            exp.getObjective().equals(configExpDto.getObjective()) &&
                            exp.getDefaultGrammar().equals(configExpDto.getFileText()) &&
                            exp.getDefaultExpDataType().equals(Long.valueOf(experimentDataTypeId)) &&
                            exp.isDe() == configExpDto.isDe() &&
                            exp.getUpperBoundDE().equals(configExpDto.getUpperBoundDE()) &&
                            exp.getLowerBoundDE().equals(configExpDto.getLowerBoundDE()) &&
                            exp.getRecombinationFactorDE().equals(configExpDto.getRecombinationFactorDE()) &&
                            exp.getMutationFactorDE().equals(configExpDto.getMutationFactorDE()) &&
                            exp.getPopulationDE().equals(configExpDto.getPopulationDE());
        }


        // END - Experiment section
        exp = this.experimentSectionService(exp, user, testExperimentDataType, expDataType, configExpDto, configExpDto.getFileText(), !sameExp);

        List<Run> runList = exp.getIdRunList();

        experimentService.saveExperiment(exp);
        this.fillConfigExpDtoService(configExpDto, exp, exp.getDefaultGrammar(), expDataType, false);

        this.modelAddDataService(model, user,
                experimentService.findExperimentDataTypeById(Long.valueOf(experimentDataTypeId)),
                exp.getIdExpDataTypeList(), testExperimentDataType == null ? null : testExperimentDataType.getId());

        model.addAttribute(EXPCONFIG, configExpDto);
        model.addAttribute(RUNLIST, runList);
        return CONFIGEXPERIMENTPATH;
    }

    public String experimentRepositoryService(Model model) {
        User user = userService.getLoggedInUser();
        List<Experiment> lExperiment = experimentService.findByUserOptimized(user);
        HashMap<Long,List<IRunDto>> lRuns = new HashMap<>();
        for (Experiment exp : lExperiment) {
            lRuns.put(exp.getId(),runService.findRunsByExpId(exp.getId()));
        }
        model.addAttribute("experimentList", lExperiment);
        model.addAttribute("user", user);
        model.addAttribute(RUNLIST, lRuns);
        return "experiment/experimentRepository";
    }

    public String expRepoSelectedService(Model model, String id) {

        User user = userService.getLoggedInUser();

        if (id == null)
            return "redirect:experiment/experimentRepository";

        Experiment exp = experimentService.findExperimentById(Long.parseLong(id));

        List<Run> runList = exp.getIdRunList();

        ConfigExperimentDto configExpDto = this.fillConfigExpDtoService(new ConfigExperimentDto(), exp,
                exp.getDefaultGrammar(), experimentService.findExperimentDataTypeById(exp.getDefaultExpDataType()),
                false);

        this.modelAddDataService(model, user, experimentService.findExperimentDataTypeById(exp.getDefaultExpDataType()),
                exp.getIdExpDataTypeList(), exp.getDefaultTestExpDataTypeId());
        model.addAttribute(CONFIGURATION, configExpDto);
        model.addAttribute("configExp", configExpDto);
        model.addAttribute(RUNLIST, runList);

        return CONFIGEXPERIMENTPATH;
    }

    public Boolean expRepoSelectedCheckRunningService(String experimentId) {
        Long idExp = Long.parseLong(experimentId);
        Experiment expConfig = experimentService.findExperimentById(idExp);
        Iterator<Run> listRunIt = expConfig.getIdRunList().iterator();

        while (listRunIt.hasNext()) {
            Run runIt = listRunIt.next();
            if(runIt == null) {
                return false;
            }
            if (runIt.getStatus().equals(Run.Status.RUNNING)) {
                return true;
            }
        }
        return false;
    }

    public RunResultsDto getRunResultsInfoService(String expId) {
        Experiment experiment = experimentService.findExperimentById(Long.valueOf(expId));
        boolean haveTest = experiment.getDefaultTestExpDataTypeId() != null;
        RunResultsDto runResultsDto = new RunResultsDto(experiment.getIdRunList().size(),
                haveTest || experiment.isCrossExperiment());
        int index = 0;
        for (Run run : experiment.getIdRunList()) {

            runResultsDto.getRunIndex()[index] = index + 1;
            runResultsDto.getModel()[index] = run.getModel();

            Map<String, List<Double>> results = this.collectTrainingAndTestStats(run,true);

            if (run.getModel() != null && !run.getModel().isEmpty()) {
                List<Double> trainingResult = results.get(TRAININGRESULT);
                runResultsDto.getTrainingRMSE()[index] = trainingResult.get(0);
                runResultsDto.getTrainingAVG()[index] = trainingResult.get(1);
                runResultsDto.getTrainingR2()[index] = trainingResult.get(2);
                runResultsDto.getTrainingAbs()[index] = trainingResult.get(3);
                runResultsDto.getTrainingRel()[index] = trainingResult.get(4);

                if (haveTest || experiment.isCrossExperiment()) {
                    List<Double> testResult = results.get(TESTRESULT);
                    runResultsDto.getTestRMSE()[index] = testResult.get(0);
                    runResultsDto.getTestAVG()[index] = testResult.get(1);
                    runResultsDto.getTestR2()[index] = testResult.get(2);
                    runResultsDto.getTestAbs()[index] = testResult.get(3);
                    runResultsDto.getTestRel()[index] = testResult.get(4);

                }
            }
            index++;
        }
        return runResultsDto;
    }

    public Map<String,String[][]> getExperimentPredictionsService(String expId) {
        // For cross-validation experiments, the full evaluation of training is returned. Hence,
        // test is only calculated if a test file is selected.
        Experiment experiment = experimentService.findExperimentById(Long.valueOf(expId));
        boolean haveTest = experiment.getDefaultTestExpDataTypeId() != null;

        ArrayList<String> models = new ArrayList<>();

        List<Double> trainingTarget = null;
        List<List<Double>> trainingPreds = new ArrayList<>();
        List<List<Double>> trainingStats = new ArrayList<>();

        List<Double> testTarget = null;
        List<List<Double>> testPreds = new ArrayList<>();
        List<List<Double>> testStats = new ArrayList<>();

        // For each model, information is retrieved.
        for (Run run : experiment.getIdRunList()) {
            Map<String, List<Double>> results = this.collectTrainingAndTestStats(run,false);

            if (trainingTarget == null) {
                trainingTarget = results.get(LISTYLINE);
            }

            if (run.getModel() != null && !run.getModel().isEmpty()) {

                models.add(run.getModel());
                trainingPreds.add(results.get(LISTFUNCTIONRESULT));
                trainingStats.add(results.get(TRAININGRESULT));

                if (haveTest) {
                    if (testTarget == null) {
                        testTarget = results.get(TESTLISTYLINE);
                    }
                    testPreds.add(results.get(TESTLISTFUNCTIONRESULT));
                    testStats.add(results.get(TESTRESULT));
                }
            }
        }

        // Return map with two matrices: training and test
        Map<String,String[][]> finalResults = new HashMap<>();
        // Rows are training size + header + 5 stats.
        // Columns are models + target, which is the first one.
        String[][] trainingResults = this.fillInResultsAndStatsService("Training",trainingTarget,trainingPreds,trainingStats,models);
        finalResults.put("training",trainingResults);

        if (haveTest) {
            String[][] testResults = this.fillInResultsAndStatsService("Test",testTarget,testPreds,testStats,models);
            finalResults.put("test", testResults);
        }

        return finalResults;
    }

    private String[][] fillInResultsAndStatsService(String label,List<Double> target, List<List<Double>> predictions, List<List<Double>> stats, ArrayList<String> models) {

        if (target == null) return new String[0][0];
        String[][] results = new String[target.size()+6][models.size()+1];
        results[0][0] = label + "Target";
        for (int i=1; i <= target.size(); i++)
            results[i][0] = String.valueOf(target.get(i - 1));
        // Add stats headers:
        results[target.size()+1][0] = "RMSE";
        results[target.size()+2][0] = "Avg. Error";
        results[target.size()+3][0] = "R2";
        results[target.size()+4][0] = "Absolute Error";
        results[target.size()+5][0] = "Relative Error";

        for (int j = 0; j < models.size(); j++) {
            results[0][j+1] = models.get(j);
            for (int i=1; i <= predictions.get(j).size(); i++)
                results[i][j+1] = String.valueOf(predictions.get(j).get(i-1));
            // Add stats:
            for (int i=0; i<stats.get(j).size(); i++)
                results[predictions.get(j).size()+1+i][j+1] = String.valueOf(stats.get(j).get(i));
        }
        return results;
    }


    private String expPropertiesSet(ConfigExperimentDto configExpDto,
                                    User user, Dataset expDataType, String grammarFilePath) throws IOException {
        // Reader - FILE DATA TYPE - Convert MultipartFile into Generic Java File - Then convert it to Reader
        File dir = new File(PROPERTIES_DIR_PATH + user.getId());
        if (!dir.exists())
            dir.mkdirs();

        String propertiesFilePath = PROPERTIES_DIR_PATH + user.getId() + File.separator + configExpDto.getExperimentName().replaceAll("\\s+", "") + "_" + UUID.randomUUID() + ".properties";

        // Write in property file
        this.createPropertiesFile(propertiesFilePath, configExpDto.getExperimentName(),
                configExpDto, user, grammarFilePath, DATATYPE_DIR_PATH + "training\\" + user.getId(), expDataType);
        // END - Create ExpPropertiesDto file

        // Execute program with experiment info

        File propertiesFile = new File(propertiesFilePath);
        // Try-with-resources does not need closing stream
        try (Reader propertiesReader = new FileReader(propertiesFile)) {

            Properties properties = new Properties();
            properties.load(propertiesReader);

            properties.setProperty(TRAINING_PATH_PROP, "");

        }
        return propertiesFilePath;
    }

    public String showRunTestStatsExperimentService(Model model,
                                                    @RequestParam(value = RUNID) String runId) {
        Run run = runService.findByRunId(Long.parseLong(runId));

        Map<String, List<Double>> results = this.collectTrainingAndTestStats(run,true);


        List<Double> listYLine = results.get(LISTYLINE);
        List<Double> listFunctionResult = results.get(LISTFUNCTIONRESULT);
        List<Double> trainingResult = results.get(TRAININGRESULT);

        model.addAttribute(EXPDETAILS, run.getExperimentId());
        model.addAttribute(LISTYLINE, listYLine);
        model.addAttribute(LISTFUNCTIONRESULT, listFunctionResult);
        model.addAttribute("RMSE", trainingResult.get(0));
        model.addAttribute("AvgError", trainingResult.get(1));
        model.addAttribute("RSquare", trainingResult.get(2));
        model.addAttribute("absoluteError", trainingResult.get(3));
        model.addAttribute("relativeError", trainingResult.get(4));
        model.addAttribute(INDEX, run.getExperimentId().getIdRunList().indexOf(run) + 1);
        model.addAttribute("model", run.getModel());

        if (run.getExperimentId().getDefaultTestExpDataTypeId() != null || run.getExperimentId().isCrossExperiment()) {
            List<Double> testResult = results.get(TESTRESULT);
            List<Double> testListYLine = results.get(TESTLISTYLINE);
            List<Double> testListFunctionResult = results.get(TESTLISTFUNCTIONRESULT);

            model.addAttribute("testRMSE", testResult.get(0));
            model.addAttribute("testAvgError", testResult.get(1));
            model.addAttribute("testRSquare", testResult.get(2));
            model.addAttribute("testAbsoluteError", testResult.get(3));
            model.addAttribute("testRelativeError", testResult.get(4));
            model.addAttribute(TESTLISTYLINE, testListYLine);
            model.addAttribute(TESTLISTFUNCTIONRESULT, testListFunctionResult);

        } else {
            model.addAttribute("noTest", true);
        }
        return "experiment/showTestStatsPlot";
    }
}