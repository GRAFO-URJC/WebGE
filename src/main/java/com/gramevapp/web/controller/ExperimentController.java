package com.gramevapp.web.controller;

import com.engine.algorithm.RunnableExpGramEv;
import com.engine.algorithm.SymbolicRegressionGE;
import com.engine.util.UtilStats;
import com.gramevapp.web.model.*;
import com.gramevapp.web.repository.GrammarRepository;
import com.gramevapp.web.service.DiagramDataService;
import com.gramevapp.web.service.ExperimentService;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import net.sourceforge.jeval.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.engine.util.Common.TRAINING_PATH_PROP;

@Controller
public class ExperimentController {

    private HashMap<Long, Thread> threadMap = new HashMap<>();
    private static HashMap<String, Long> threadRunMap = new HashMap<>();
    private static HashMap<Long, RunnableExpGramEv> runnables = new HashMap<>();
    private static final String LOGGER_BASE_PATH = "resources/files/logs/population";

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    @Autowired
    private RunService runService;

    @Autowired
    private DiagramDataService diagramDataService;

    @Autowired
    private GrammarRepository grammarRepository;

    @ModelAttribute
    public FileModelDto fileModel() {
        return new FileModelDto();
    }

    private final String GRAMMAR_DIR_PATH = "." + File.separator + "resources" + File.separator + "files" + File.separator + "grammar" + File.separator + "";
    private final String DATATYPE_DIR_PATH = "." + File.separator + "resources" + File.separator + "files" + File.separator + "dataType" + File.separator + "";
    private final String PROPERTIES_DIR_PATH = "." + File.separator + "resources" + File.separator + "files" + File.separator + "properties" + File.separator + "";
    private static final String WORK_DIR = "resources/files";
    private static final String CLASS_PATH_SEPARATOR = "\\;";

    private void modelAddData(Model model, User user, Dataset experimentDataType,
                              List<Dataset> experimentDataTypeList, Long testExperimentDataTypeId) {
        Dataset testExperimentDataType = testExperimentDataTypeId == null ? null : experimentService.findDataTypeById(testExperimentDataTypeId);
        model.addAttribute("grammarList", grammarRepository.findByUserId(user.getId()));
        model.addAttribute("datasetList", experimentService.findAllExperimentDataTypeByUserId(user.getId()));
        model.addAttribute("experimentDataType", experimentDataType);
        model.addAttribute("testExperimentDataType", testExperimentDataType);
        model.addAttribute("dataTypeList", experimentDataTypeList);
    }

    @GetMapping("/experiment/configExperiment")
    public String configExperiment(Model model,
                                   @ModelAttribute("configuration") ConfigExperimentDto configExpDto) {
        User user = userService.getLoggedInUser();

        model.addAttribute("type", new Dataset());
        model.addAttribute("configuration", configExpDto);
        model.addAttribute("runList", null);
        model.addAttribute("user", user);
        model.addAttribute("configExp", new ConfigExperimentDto());
        model.addAttribute("disabledClone", true);
        modelAddData(model, user, null, null, null);

        return "experiment/configExperiment";
    }

    /**
     * Load al the data from the view, save it and run the application.
     * "configExpDto" for validation -> configExp
     * "configuration" is for send data from Controller to View and
     * "configExp" is the object from the form View
     */
    @RequestMapping(value = "/experiment/start", method = RequestMethod.POST, params = "runExperimentButton")
    public String runExperiment(Model model,
                                @RequestParam("experimentDataTypeId") String experimentDataTypeId,
                                @RequestParam("testExperimentDataTypeId") String testExperimentDataTypeId,
                                @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto,
                                BindingResult result,
                                RedirectAttributes redirectAttrs) throws IllegalStateException, IOException {

        User user = userService.getLoggedInUser();
        modelAddData(model, user, null, null, null);

        // Check the data received
        if (result.hasErrors()) {
            model.addAttribute("configuration", configExpDto);
            return "experiment/configExperiment";
        }

        // Experiment Data Type SECTION
        Dataset expDataType = experimentService.
                findExperimentDataTypeById(Long.valueOf(experimentDataTypeId));

        experimentDataTypeSection(expDataType);
        // END - Experiment Data Type SECTION

        // Experiment section:
        Experiment exp = experimentSection(configExpDto.getId() != null ?
                        experimentService.findExperimentById(configExpDto.getId()) : null
                , user,
                (testExperimentDataTypeId.equals("")) ? null : experimentService.
                        findExperimentDataTypeById(Long.valueOf(testExperimentDataTypeId))
                , expDataType, configExpDto, configExpDto.getFileText());
        experimentService.saveExperiment(exp);
        // END - Experiment section

        // Grammar File SECTION
        String grammarFilePath = grammarFileSection(user, configExpDto, exp.getDefaultGrammar());
        // END - Grammar File SECTION

        Run run;
        String propPath;

        List<Thread> threads = new ArrayList<>();
        //check if need to run more runs
        for (int i = 0; i < configExpDto.getNumberRuns(); i++) {
            // RUN SECTION
            run = runService.saveRun(new Run());
            runSection(run, exp);
            run.setStatus(Run.Status.WAITING);
            // Create ExpPropertiesDto file
            propPath = expPropertiesSet(configExpDto,
                    user, expDataType, grammarFilePath);
            // Run experiment in new thread
            threads.add(runExperimentDetails(run, propPath, exp.isCrossExperiment() ? (i + 1) / expDataType.getFoldSize() : -1));
        }
        experimentService.saveExperiment(exp);

        Thread thread = new Thread(() -> {
            try {
                for (Thread th : threads) {
                    th.start();
                    th.join();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();

        redirectAttrs.addAttribute("id", exp.getId());
        redirectAttrs.addAttribute("loadExperimentButton", "loadExperimentButton");
        return "redirect:/experiment/expRepoSelected";
    }

    protected String expPropertiesSet(ConfigExperimentDto configExpDto,
                                      User user, Dataset expDataType, String grammarFilePath) throws IOException {
        // Reader - FILE DATA TYPE - Convert MultipartFile into Generic Java File - Then convert it to Reader
        File dir = new File(PROPERTIES_DIR_PATH + user.getId());
        if (!dir.exists())
            dir.mkdirs();

        String propertiesFilePath = PROPERTIES_DIR_PATH + user.getId() + File.separator + configExpDto.getExperimentName().replaceAll("\\s+", "") + "_" + UUID.randomUUID() + ".properties";
        createPropertiesFile(propertiesFilePath, configExpDto.getExperimentName(),
                configExpDto, user, grammarFilePath, DATATYPE_DIR_PATH + "training\\" + user.getId(), expDataType);  // Write in property file
        // END - Create ExpPropertiesDto file

        // Execute program with experiment info
        File propertiesFile = new File(propertiesFilePath);
        Reader propertiesReader = new FileReader(propertiesFile);

        Properties properties = new Properties();
        properties.load(propertiesReader);

        properties.setProperty(TRAINING_PATH_PROP, "");
        propertiesReader.close();
        return propertiesFilePath;
    }


    @RequestMapping(value = "/experiment/start", method = RequestMethod.POST, params = "saveExperimentButton")
    public String saveExperiment(Model model,
                                 @RequestParam("experimentDataTypeId") String experimentDataTypeId,
                                 @RequestParam("testExperimentDataTypeId") String testExperimentDataTypeId,
                                 @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                 @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto,
                                 BindingResult result) throws IllegalStateException, IOException {

        User user = userService.getLoggedInUser();
        modelAddData(model, user, null, null, null);
        model.addAttribute("configuration", configExpDto);

        if (result.hasErrors()) {
            return "experiment/configExperiment";
        }

        Experiment exp = null;

        // Experiment Data Type SECTION
        Dataset expDataType;
        if (experimentDataTypeId.equals("-1")) {
            model.addAttribute("configuration", configExpDto);
            model.addAttribute("expConfig", configExpDto);
            result.rejectValue("typeFile", "error.typeFile", "Choose one file");
            return "experiment/configExperiment";
        } else {
            expDataType = experimentService.findDataTypeById(Long.parseLong(experimentDataTypeId));
        }

        experimentDataTypeSection(expDataType);
        // END - Experiment Data Type SECTION

        // Experiment section:
        Dataset testExperimentDataType = (testExperimentDataTypeId.equals("")) ? null : experimentService.
                findExperimentDataTypeById(Long.valueOf(testExperimentDataTypeId));

        if (configExpDto.getId() != null) {
            exp = experimentService.findExperimentById(configExpDto.getId());
            configExpDto = fillConfigExpDto(configExpDto, exp,
                    exp.getDefaultGrammar(), expDataType);
            // check if only test was changed
            boolean sameExp = exp.getExperimentName().equals(configExpDto.getExperimentName()) &&
                    exp.getExperimentDescription().equals(configExpDto.getExperimentDescription()) &&
                    exp.getGenerations().equals(configExpDto.getGenerations()) &&
                    exp.getCrossoverProb().equals(configExpDto.getCrossoverProb()) &&
                    exp.getPopulationSize().equals(configExpDto.getPopulationSize()) &&
                    exp.getMutationProb().equals(configExpDto.getMutationProb()) &&
                    exp.getMaxWraps().equals(configExpDto.getMaxWraps()) &&
                    exp.getTournament().equals(configExpDto.getTournament()) &&
                    exp.getNumberRuns().equals(configExpDto.getNumberRuns()) &&
                    exp.getObjective().equals(configExpDto.getObjective()) &&
                    exp.getDefaultGrammar().equals(configExpDto.getFileText()) &&
                    exp.getDefaultExpDataType().equals(Long.valueOf(experimentDataTypeId));
            if (sameExp) {
                if (!testExperimentDataTypeId.equals("")) {
                    exp.setDefaultTestExpDataTypeId(Long.valueOf(testExperimentDataTypeId));
                } else {
                    exp.setDefaultTestExpDataTypeId(null);
                }
                exp.setModificationDate(new Timestamp(new Date().getTime()));
                experimentService.saveExperiment(exp);
                modelAddData(model, user,
                        experimentService.findExperimentDataTypeById(Long.valueOf(experimentDataTypeId)),
                        exp.getIdExpDataTypeList(), testExperimentDataType == null ? null : testExperimentDataType.getId());

                model.addAttribute("runList", exp.getIdRunList());
                return "experiment/configExperiment";
            }
        }


        exp = experimentSection(exp, user, testExperimentDataType, expDataType, configExpDto, configExpDto.getFileText());
        // END - Experiment section

        removeRuns(exp);
        experimentService.saveExperiment(exp);
        fillConfigExpDto(configExpDto, exp, exp.getDefaultGrammar(), expDataType);

        modelAddData(model, user,
                experimentService.findExperimentDataTypeById(Long.valueOf(experimentDataTypeId)),
                exp.getIdExpDataTypeList(), testExperimentDataType == null ? null : testExperimentDataType.getId());

        model.addAttribute("expConfig", configExpDto);
        return "experiment/configExperiment";

    }

    @RequestMapping(value = "/experiment/start", method = RequestMethod.POST, params = "cloneExperimentButton")
    public String cloneExperiment(Model model,
                                  @RequestParam("experimentDataTypeId") String experimentDataTypeId,
                                  @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                  @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto) throws IllegalStateException {
        User user = userService.getLoggedInUser();
        configExpDto.setId(null);

        model.addAttribute("configuration", configExpDto);
        model.addAttribute("expConfig", configExpDto);
        modelAddData(model, user,
                experimentService.findExperimentDataTypeById(Long.valueOf(experimentDataTypeId)),
                null, configExpDto.getTestDefaultExpDataTypeId());
        model.addAttribute("disabledClone", true);
        model.addAttribute("messageClone", "This experiment is cloned and not saved yet.");
        return "experiment/configExperiment";
    }

    @RequestMapping(value = "/experiment/experimentRepository", method = RequestMethod.GET)
    public String experimentRepository(Model model) {

        User user = userService.getLoggedInUser();
        List<Experiment> lExperiment = experimentService.findByUser(user);
        model.addAttribute("experimentList", lExperiment);
        model.addAttribute("user", user);

        return "experiment/experimentRepository";
    }

    @RequestMapping(value = "/experiment/expRepoSelected", method = RequestMethod.GET, params = "loadExperimentButton")
    public String expRepoSelected(Model model,
                                  @RequestParam(required = false) String id) { // Exp ID

        User user = userService.getLoggedInUser();

        if (id == null)
            return "redirect:experiment/experimentRepository";

        Experiment exp = experimentService.findExperimentById(Long.parseLong(id));

        List<Run> runList = exp.getIdRunList();

        ConfigExperimentDto configExpDto = fillConfigExpDto(new ConfigExperimentDto(), exp,
                exp.getDefaultGrammar(), experimentService.findExperimentDataTypeById(exp.getDefaultExpDataType()));

        modelAddData(model, user, experimentService.findExperimentDataTypeById(exp.getDefaultExpDataType()),
                exp.getIdExpDataTypeList(), exp.getDefaultTestExpDataTypeId());
        model.addAttribute("configuration", configExpDto);
        model.addAttribute("configExp", configExpDto);
        model.addAttribute("runList", runList);

        return "experiment/configExperiment";
    }

    @RequestMapping(value = "/experiment/expRepoSelected", method = RequestMethod.POST, params = "deleteExperiment")
    public
    @ResponseBody
    Long expRepoSelectedDelete(@RequestParam("experimentId") String experimentId) {
        Long idExp = Long.parseLong(experimentId);

        Experiment expConfig = experimentService.findExperimentById(idExp);

        Iterator<Run> listRunIt = expConfig.getIdRunList().iterator();
        while (listRunIt.hasNext()) {
            Run runIt = listRunIt.next();
            Long threadId = runIt.getThreaId();
            // https://stackoverflow.com/questions/26213615/terminating-thread-using-thread-id-in-java
            Thread th = threadMap.get(threadId);
            if (th != null) {
                runIt.setStatus(Run.Status.STOPPED);
                runService.saveRun(runIt);

                th.interrupt();
                runnables.get(threadId).stopExecution();
            }
            listRunIt.remove();
            runIt.setExperimentId(null);
            runService.removeExecutionReport(runService.getRunExecutionReport(runIt.getId()));
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

    @GetMapping(value = "/experiment/runList", params = "showPlotExecutionButton")
    public String showPlotExecutionExperiment(Model model,
                                              @RequestParam(value = "runId") String runId) {
        Long longRunId = Long.parseLong(runId);
        Run run = runService.findByRunId(longRunId);

        model.addAttribute("expDetails", run.getExperimentId());
        model.addAttribute("runId", run.getId());
        model.addAttribute("run", run);
        model.addAttribute("index", run.getExperimentId().getIdRunList().indexOf(run) + 1);

        return "experiment/experimentDetails";
    }

    @GetMapping(value = "/experiment/runList", params = "showTestStatsPlotButton")
    public String showRunTestStatsExperiment(Model model,
                                             @RequestParam(value = "runId") String runId) throws EvaluationException {
        Run run = runService.findByRunId(Long.parseLong(runId));
        int crossRunIdentifier = run.getExperimentId().getIdRunList().indexOf(run) + 1;
        List<Double> listYLine = new ArrayList<>();
        List<Double> listFunctionResult = new ArrayList<>();
        List<Double> trainingResult = new ArrayList<>();
        String[] splitContent =
                experimentService.findExperimentDataTypeById(run.getExperimentId().getDefaultExpDataType()).getInfo().split("\r\n");
        String[] testSplitContent = null;

        if (run.getExperimentId().isCrossExperiment()) {
            List<String> splitContentList = new ArrayList<>();
            List<String> testSplitContentList = new ArrayList<>();
            splitContent[0] = splitContent[0].substring(0, splitContent[0].length() - ";K-Fold".length()) + "\r\n";
            splitContentList.add(splitContent[0]);
            testSplitContentList.add(splitContent[0]);
            //index last ;
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

        model.addAttribute("expDetails", run.getExperimentId());
        model.addAttribute("listYLine", listYLine);
        model.addAttribute("listFunctionResult", listFunctionResult);
        model.addAttribute("RMSE", trainingResult.get(0));
        model.addAttribute("AvgError", trainingResult.get(1));
        model.addAttribute("RSquare", trainingResult.get(2));
        model.addAttribute("absoluteError", trainingResult.get(3));
        model.addAttribute("index", run.getExperimentId().getIdRunList().indexOf(run) + 1);
        model.addAttribute("model", run.getModel());

        if (run.getExperimentId().getDefaultTestExpDataTypeId() != null || run.getExperimentId().isCrossExperiment()) {
            List<Double> testListYLine = new ArrayList<>();
            List<Double> testListFunctionResult = new ArrayList<>();
            List<Double> testResult = new ArrayList<>();
            if (run.getExperimentId().isCrossExperiment()) {
                splitContent = testSplitContent;
            } else {
                splitContent = experimentService.findExperimentDataTypeById(run.getExperimentId().getDefaultTestExpDataTypeId()).getInfo().split("\r\n");
            }

            processExperimentDataTypeInfo(splitContent, testListYLine, testListFunctionResult, testResult, run);
            model.addAttribute("testRMSE", testResult.get(0));
            model.addAttribute("testAvgError", testResult.get(1));
            model.addAttribute("testRSquare", testResult.get(2));
            model.addAttribute("testAbsoluteError", testResult.get(3));
            model.addAttribute("testListYLine", testListYLine);
            model.addAttribute("testListFunctionResult", testListFunctionResult);

        } else {
            model.addAttribute("noTest", true);
        }

        return "experiment/showTestStatsPlot";
    }

    private void processExperimentDataTypeInfo(String[] splitContent, List<Double> listYLine, List<Double> listFunctionResult, List<Double> result,
                                               Run run) throws EvaluationException {
        double[] yDoubleArray = new double[splitContent.length - 1];
        double[] functionResultDoubleArray = new double[splitContent.length - 1];
        double yValue, modelValue;

        for (int i = 1; i < splitContent.length; i++) {
            String[] contentSplit = splitContent[i].split(";");
            yValue = Double.parseDouble(contentSplit[0]);
            modelValue = SymbolicRegressionGE.calculateFunctionValuedResultWithCSVData(run.getModel(),
                    contentSplit);
            if (listYLine != null) {
                listYLine.add(yValue);
            }
            if (listFunctionResult != null) {
                listFunctionResult.add(modelValue);
            }
            yDoubleArray[i - 1] = yValue;
            functionResultDoubleArray[i - 1] = modelValue;
        }
        // RMSE AVGERROR RSQUARE ABSOLUTEERROR
        result.add(UtilStats.computeRMSE(yDoubleArray, functionResultDoubleArray));
        result.add(UtilStats.computeAvgError(yDoubleArray, functionResultDoubleArray));
        result.add(UtilStats.computeRSquare(yDoubleArray, functionResultDoubleArray));
        result.add(UtilStats.computeAbsoluteError(yDoubleArray, functionResultDoubleArray));
    }

    private Thread runExperimentDetails(Run run, String propPath, int crossRunIdentifier) throws IOException {
        File propertiesFile = new File(propPath);
        Reader propertiesReader = new FileReader(propertiesFile);

        Properties properties = new Properties();
        properties.load(propertiesReader);
        properties.setProperty(TRAINING_PATH_PROP, propPath);
        RunnableExpGramEv obj = new RunnableExpGramEv(properties, run,
                experimentService.findExperimentDataTypeById(run.getExperimentId().getDefaultExpDataType()), runService,
                crossRunIdentifier);
        Thread.UncaughtExceptionHandler h = (th, ex) -> {
            run.setStatus(Run.Status.FAILED);
            RunExecutionReport runExecutionReport = runService.getRunExecutionReport(run.getId());
            runExecutionReport.setExecutionReport(runExecutionReport.getExecutionReport() + "\nUncaught exception: " + ex);
            runService.saveRunExecutionReport(runExecutionReport);
            System.out.println(("Uncaught exception: " + ex));
        };
        Thread th = new Thread(obj);
        th.setUncaughtExceptionHandler(h);
        threadMap.put(th.getId(), th);
        threadRunMap.put(th.getName(), run.getId());
        run.setThreaId(th.getId());
        runnables.put(th.getId(), obj);
        // https://stackoverflow.com/questions/26213615/terminating-thread-using-thread-id-in-java

        propertiesReader.close();
        return th;
    }

    private void createPropertiesFile(String propertiesFilePath, String expName,
                                      ConfigExperimentDto configExpDto, User user, String grammarFilePath,
                                      String dataTypeDirectoryPath, Dataset expDataType) throws IOException {
        File propertiesNewFile = new File(propertiesFilePath);
        if (!propertiesNewFile.exists()) {
            propertiesNewFile.createNewFile();
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

        propertiesWriter.close();
    }

    private void runSection(Run run, Experiment exp) {
        run.setStatus(Run.Status.INITIALIZING);
        run.setIniDate(new Timestamp(new Date().getTime()));
        run.setModificationDate(new Timestamp(new Date().getTime()));
        exp.getIdRunList().add(run);
        run.setExperimentId(exp);

        RunExecutionReport runExecutionReport = new RunExecutionReport();
        runExecutionReport.setId(run.getId());
        runService.saveRunExecutionReport(runExecutionReport);

    }

    private String grammarFileSection(User user, ConfigExperimentDto configExpDto, String grammar) throws IllegalStateException, IOException {

        File dir = new File(GRAMMAR_DIR_PATH + user.getId());
        if (!dir.exists())
            dir.mkdirs();

        String grammarFilePath = GRAMMAR_DIR_PATH + user.getId() + File.separator + configExpDto.getExperimentName().replaceAll("\\s+", "") + "_" +
                grammarRepository.getNextValue() + ".bnf";

        File grammarNewFile = new File(grammarFilePath);
        if (!grammarNewFile.exists()) {
            grammarNewFile.createNewFile();
        }

        PrintWriter grammarWriter = new PrintWriter(grammarNewFile);

        String[] parts = grammar.split("\\r\\n");
        for (String part : parts) {
            grammarWriter.println(part);
        }

        grammarWriter.close();

        return grammarFilePath;
    }

    private void experimentDataTypeSection(Dataset expDataType) {
        expDataType.setCreationDate(new Timestamp(new Date().getTime()));
        expDataType.setDataTypeType("training");
    }

    private Experiment experimentSection(Experiment exp, User user, Dataset testExpDataType,
                                         Dataset expDataType,
                                         ConfigExperimentDto configExpDto, String grammar) {
        if (exp == null) {   // We create it
            exp = new Experiment(user, configExpDto.getExperimentName(), configExpDto.getExperimentDescription(), configExpDto.getGenerations(),
                    configExpDto.getPopulationSize(), configExpDto.getMaxWraps(), configExpDto.getTournament(), configExpDto.getCrossoverProb(), configExpDto.getMutationProb(),
                    configExpDto.getNumCodons(), configExpDto.getNumberRuns(), configExpDto.getObjective(),
                    new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime()));

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

            exp.setCreationDate(new Timestamp(new Date().getTime()));
            exp.setModificationDate(new Timestamp(new Date().getTime()));

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

    private void removeRuns(Experiment exp) {
        List<Run> oldRunList = new ArrayList<>(exp.getIdRunList());
        //remove old run
        for (Run oldRun : oldRunList) {
            Thread th = threadMap.get(oldRun.getThreaId());
            if (th != null && th.isAlive()) {
                th.interrupt();
                runnables.get(oldRun.getThreaId()).stopExecution();
            }
            runService.removeExecutionReport(runService.getRunExecutionReport(oldRun.getId()));
            exp.removeRun(oldRun);
            runService.deleteRun(oldRun);
        }
    }

    @PostMapping(value = "/experiment/stopRun", params = "stopRunExperimentButton")
    public String stopRunExperiment(Model model,
                                    @RequestParam("runIdStop") String runIdStop,
                                    RedirectAttributes redirectAttrs) throws InterruptedException {
        Run run = runService.findByRunId(Long.parseLong(runIdStop));
        Long threadId = run.getThreaId();

        // https://stackoverflow.com/questions/26213615/terminating-thread-using-thread-id-in-java
        Thread th = threadMap.get(threadId);
        if (th == null) {
            run.setStatus(Run.Status.FAILED);

            if (redirectAttrs != null) {
                redirectAttrs.addAttribute("runId", run.getId()).addFlashAttribute("Stop", "Stop execution failed");
                redirectAttrs.addAttribute("showPlotExecutionButton", "showPlotExecutionButton");
            }
            return "redirect:experiment/runList";
        }
        th.interrupt();
        runnables.get(threadId).stopExecution();
        th.join();
        run = runService.findByRunId(Long.parseLong(runIdStop));
        run.getDiagramData().setStopped(true);
        diagramDataService.saveDiagram(run.getDiagramData());

        run.setStatus(Run.Status.STOPPED);
        runService.saveRun(run);

        if (model != null) {
            model.addAttribute("expDetails", run.getExperimentId());
            model.addAttribute("runId", run.getId());
            model.addAttribute("run", run);
            model.addAttribute("index", run.getExperimentId().getIdRunList().indexOf(run) + 1);
        }

        return "experiment/experimentDetails";
    }

    @PostMapping(value = "/experiment/stopRunAjax")
    @ResponseBody
    public boolean ajaxStopRunExperiment(@RequestParam("runIdStop") String runIdStop) throws InterruptedException {
        this.stopRunExperiment(null, runIdStop, null);
        return true;
    }

    @RequestMapping(value = "/experiment/expRepoSelected", method = RequestMethod.POST, params = "deleteRun")
    public
    @ResponseBody
    Long deleteRun(@RequestParam("runId") String runId) {
        Long longRunId = Long.parseLong(runId);
        Run run = runService.findByRunId(longRunId);
        runService.deleteRun(run);
        return longRunId;
    }

    public ConfigExperimentDto fillConfigExpDto(ConfigExperimentDto configExpDto, Experiment exp, String grammar,
                                                Dataset dataset) {

        setConfigExpDtoWIthExperiment(configExpDto, exp.getExperimentName(),
                exp.getExperimentDescription(), exp.getCrossoverProb(), exp.getGenerations(),
                exp.getPopulationSize(), exp.getMaxWraps(), exp.getTournament(), exp.getMutationProb(),
                exp.getNumCodons(), exp.getNumberRuns(), exp.getObjective());

        configExpDto.setId(exp.getId());
        configExpDto.setDefaultExpDataTypeId(exp.getDefaultExpDataType());
        configExpDto.setFileText(grammar);
        configExpDto.setCrossExperiment(exp.isCrossExperiment() ? "true" : "false");
        configExpDto.setContentFold(dataset.getInfo().contains("K-Fold"));

        return configExpDto;
    }

    private void setConfigExpDtoWIthExperiment(ConfigExperimentDto configExpDto, String experimentName, String experimentDescription,
                                               Double crossoverProb, Integer generations, Integer populationSize, Integer maxWraps,
                                               Integer tournament, Double mutationProb,
                                               Integer numCodons, Integer numberRuns, String objective) {
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
    }

    public static HashMap<Long, RunnableExpGramEv> getRunnables() {
        return runnables;
    }

    @PostConstruct
    public void initSystemStream() {
        String messageSkip = "\u001B[0;39m \u001B[36mc.engine.algorithm.SymbolicRegressionGE \u001B[0;39m \u001B[2m:\u001B[0;39m ";
        System.setOut(new PrintStream(System.out) {
            @Override
            public void write(byte[] buf, int off, int len) {
                //Convert byte[] to String
                String logInfo = new String(buf);
                if (logInfo.contains("j.c.algorithm.ga.SimpleGeneticAlgorithm") ||
                        logInfo.contains("c.engine.algorithm.SymbolicRegressionGE")) {
                    String infoFormated = "";
                    Pattern pattern =
                            Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}");
                    Matcher matcher = pattern.matcher(logInfo);
                    if (matcher.find()) {
                        infoFormated += matcher.group() + (logInfo.contains("j.c.algorithm.ga.SimpleGeneticAlgorithm") ?
                                " j.c.algorithm.ga.SimpleGeneticAlgorithm :" : " c.engine.algorithm.SymbolicRegressionGE :");
                    }
                    pattern =
                            Pattern.compile("[0-9]{1,3}% performed.*$");
                    matcher = pattern.matcher(logInfo);

                    if (matcher.find()) {
                        //j.c.algorithm.ga.SimpleGeneticAlgorithm
                        infoFormated += matcher.group() + "\r\n";
                    } else {
                        //c.engine.algorithm.SymbolicRegressionGE
                        infoFormated += logInfo.substring(logInfo.indexOf(messageSkip) + messageSkip.length());
                    }
                    pattern =
                            Pattern.compile("Thread-[0-9]+");
                    matcher = pattern.matcher(logInfo);
                    if (matcher.find()) {
                        String threadName = matcher.group();
                        RunExecutionReport runExecutionReport = runService.getRunExecutionReport(threadRunMap.get(threadName));
                        if (runExecutionReport.getExecutionReport() == null) {
                            runExecutionReport.setExecutionReport("");
                        }
                        runExecutionReport.setExecutionReport(runExecutionReport.getExecutionReport() + infoFormated);
                        runService.saveRunExecutionReport(runExecutionReport);
                    }

                }

                super.write(buf, off, len);
            }
        });
    }

    @RequestMapping(value = "/runResultsInfo", method = RequestMethod.GET)
    @ResponseBody
    public RunResultsDto getRunResultsInfo(@RequestParam("expId") String expId) throws EvaluationException {
        Experiment experiment = experimentService.findExperimentById(Long.valueOf(expId));
        boolean haveTest = experiment.getDefaultTestExpDataTypeId() != null;
        RunResultsDto runResultsDto = new RunResultsDto(experiment.getIdRunList().size(),
                haveTest);
        int index = 0;
        for (Run run : experiment.getIdRunList()) {
            runResultsDto.getRunIndex()[index] = index + 1;
            runResultsDto.getModel()[index] = run.getModel();
            List<Double> result = new ArrayList<>();
            processExperimentDataTypeInfo(experimentService.findExperimentDataTypeById(experiment.getDefaultExpDataType()).getInfo().split("\r\n"),
                    null, null, result, run);
            runResultsDto.getTrainingRMSE()[index] = result.get(0);
            runResultsDto.getTrainingAVG()[index] = result.get(1);
            runResultsDto.getTrainingR2()[index] = result.get(2);
            runResultsDto.getTrainingAbs()[index] = result.get(3);

            if (haveTest) {
                result = new ArrayList<>();
                processExperimentDataTypeInfo(experimentService.findExperimentDataTypeById(experiment.getDefaultTestExpDataTypeId()).getInfo().split("\r\n"),
                        null, null, result, run);
                runResultsDto.getTestRMSE()[index] = result.get(0);
                runResultsDto.getTestAVG()[index] = result.get(1);
                runResultsDto.getTestR2()[index] = result.get(2);
                runResultsDto.getTestAbs()[index] = result.get(3);
            }
            index++;
        }
        return runResultsDto;
    }

}