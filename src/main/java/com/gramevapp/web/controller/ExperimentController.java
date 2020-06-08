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
import java.sql.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.engine.util.Common.TRAINING_PATH_PROP;

@Controller
public class ExperimentController {

    private HashMap<Long, Thread> threadMap = new HashMap<>();
    private static HashMap<String, Long> threadRunMap = new HashMap<>();
    private static HashMap<Long, RunnableExpGramEv> runnables = new HashMap<>();

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

    private void modelAddData(Model model, User user, Grammar grammar, Dataset experimentDataType,
                              List<Dataset> experimentDataTypeList, Long testExperimentDataTypeId) {
        Dataset testExperimentDataType = testExperimentDataTypeId == null ? null : experimentService.findDataTypeById(testExperimentDataTypeId);
        model.addAttribute("grammarList", grammarRepository.findByUserId(user.getId()));
        model.addAttribute("datasetList", experimentService.findAllExperimentDataTypeByUserId(user.getId()));
        model.addAttribute("grammar", grammar);
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
        modelAddData(model, user, null, null, null, null);

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
                                @RequestParam("grammarId") String grammarId,
                                @RequestParam("experimentDataTypeId") String experimentDataTypeId,
                                @RequestParam("testExperimentDataTypeId") String testExperimentDataTypeId,
                                @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto,
                                BindingResult result,
                                RedirectAttributes redirectAttrs) throws IllegalStateException, IOException {

        User user = userService.getLoggedInUser();
        modelAddData(model, user, null, null, null, null);

        // Check the data received
        if (result.hasErrors()) {
            model.addAttribute("configuration", configExpDto);
            return "experiment/configExperiment";
        }

        // CONFIGURATION SECTION
        // GRAMMAR SECTION
        Grammar grammar = grammarRepository.findGrammarById(Long.parseLong(grammarId));

        // RUN SECTION
        Run run = runService.saveRun(new Run());
        runSection(run, grammar, configExpDto);

        // Experiment Data Type SECTION
        Dataset expDataType = experimentService.
                findExperimentDataTypeById(Long.valueOf(experimentDataTypeId));

        experimentDataTypeSection(expDataType);
        run.setDefaultExpDataTypeId(expDataType.getId());
        // END - Experiment Data Type SECTION

        // Experiment section:
        Experiment exp = experimentSection(configExpDto.getId() != null ?
                        experimentService.findExperimentById(configExpDto.getId()) : null
                , user,
                (testExperimentDataTypeId.equals("")) ? null : experimentService.
                        findExperimentDataTypeById(Long.valueOf(testExperimentDataTypeId))
                , expDataType, configExpDto, grammar, run, run.getId());
        experimentService.saveExperiment(exp);
        // END - Experiment section

        // Grammar File SECTION
        String grammarFilePath = grammarFileSection(user, configExpDto, grammar);
        // END - Grammar File SECTION

        // Create ExpPropertiesDto file
        expPropertiesSet(fileModelDto, configExpDto, user, expDataType, exp, grammarFilePath, run);

        List<Thread> threads = new ArrayList<>();
        // Run experiment in new thread
        threads.add(runExperimentDetails(user, run, run.getDiagramData()));
        //check if need to run more runs
        for (int i = 1; i < configExpDto.getNumberRuns(); i++) {
            // RUN SECTION
            Run newRun = runService.saveRun(new Run());
            runSection(newRun, grammar, configExpDto);
            exp.getIdRunList().add(newRun);
            newRun.setExperimentId(exp);

            newRun.setDefaultExpDataTypeId(expDataType.getId());
            // Create ExpPropertiesDto file
            expPropertiesSet(fileModelDto, configExpDto, user, expDataType, exp, grammarFilePath, newRun);

            newRun.setStatus(Run.Status.WAITING);

            // Run experiment in new thread
            threads.add(runExperimentDetails(user, newRun, newRun.getDiagramData()));
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

        redirectAttrs.addAttribute("idRun", run.getId()).addFlashAttribute("configuration",
                "Experiment is being created");
        return "redirect:/experiment/redirectConfigExperiment";
    }

    protected void expPropertiesSet(@ModelAttribute("typeFile") FileModelDto fileModelDto, @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto, User user , Dataset expDataType, Experiment exp, String grammarFilePath, Run newRun) throws IOException {
        ExpPropertiesDto newPropertiesDto = new ExpPropertiesDto(user,
                0.0, configExpDto.getTournament(), 0,
                configExpDto.getCrossoverProb(), grammarFilePath, 0,
                1, configExpDto.getMutationProb(), false, 1,
                configExpDto.getNumCodons(), configExpDto.getPopulationSize(),
                configExpDto.getGenerations(), false, configExpDto.getMaxWraps(),
                500, configExpDto.getExperimentName(),
                configExpDto.getExperimentDescription());
        fileConfig(expDataType, user, newPropertiesDto, configExpDto,
                newRun, exp);

        DiagramData newDiagramData = new DiagramData();
        newDiagramData.setRunId(newRun);
        diagramDataService.saveDiagram(newDiagramData);
    }

    @GetMapping("/experiment/redirectConfigExperiment")
    public String redirectViewConfigExperiment(Model model,
                                               @ModelAttribute("idRun") String idRun) {

        User user = userService.getLoggedInUser();
        Run run = runService.findByRunId(Long.parseLong(idRun));

        if (run == null) { // Run eliminated
            model.addAttribute("grammar", new Grammar());
            model.addAttribute("type", new Dataset());
            model.addAttribute("configuration", new ConfigExperimentDto());
            model.addAttribute("user", user);
            return "experiment/configExperiment";
        }
        Grammar grammar = experimentService.findGrammarById(run.getExperimentId().getDefaultGrammar());
        Dataset expDataType = experimentService.findExperimentDataTypeById(run.getExperimentId().getDefaultExpDataType());
        Experiment experiment = experimentService.findExperimentById(run.getExperimentId().getId());

        List<Run> runList = run.getExperimentId().getIdRunList();
        List<Dataset> expDataTypeList = run.getExperimentId().getIdExpDataTypeList();

        ConfigExperimentDto configExpDto = fillConfigExpDto(new ConfigExperimentDto(),
                run.getExperimentId(), run, grammar);

        model.addAttribute("configuration", configExpDto);
        model.addAttribute("runList", runList);
        model.addAttribute("dataTypeList", expDataTypeList);
        model.addAttribute("configExp", configExpDto);
        modelAddData(model, user, grammar, expDataType, expDataTypeList, experiment.getDefaultTestExpDataTypeId());

        return "experiment/configExperiment";
    }

    @RequestMapping(value = "/experiment/start", method = RequestMethod.POST, params = "saveExperimentButton")
    public String saveExperiment(Model model,
                                 @RequestParam("grammarId") String grammarId,
                                 @RequestParam("experimentDataTypeId") String experimentDataTypeId,
                                 @RequestParam("testExperimentDataTypeId") String testExperimentDataTypeId,
                                 @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                 @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto,
                                 BindingResult result) throws IllegalStateException, IOException {

        User user = userService.getLoggedInUser();
        modelAddData(model, user, null, null, null, null);

        if (result.hasErrors()) {
            model.addAttribute("configuration", configExpDto);
            return "experiment/configExperiment";
        }

        Experiment exp = null;
        model.addAttribute("configuration", configExpDto);
        model.addAttribute("expConfig", configExpDto);

        // Grammar section
        Grammar updGrammar = experimentService.findGrammarById(Long.valueOf(grammarId));

        // Experiment Data Type SECTION
        Dataset expDataType;
        if (experimentDataTypeId.equals("-1")) {
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
            fillConfigExpDto(configExpDto, exp, null, updGrammar);
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
                    exp.getDefaultGrammar().equals(Long.valueOf(grammarId)) &&
                    exp.getDefaultExpDataType().equals(Long.valueOf(experimentDataTypeId));
            if (sameExp) {
                if (!testExperimentDataTypeId.equals("")) {
                    exp.setDefaultTestExpDataTypeId(Long.valueOf(testExperimentDataTypeId));
                } else {
                    exp.setDefaultTestExpDataTypeId(null);
                }
                experimentService.saveExperiment(exp);
                modelAddData(model, user, grammarRepository.findGrammarById(Long.valueOf(grammarId)),
                        experimentService.findExperimentDataTypeById(Long.valueOf(experimentDataTypeId)),
                        exp.getIdExpDataTypeList(), testExperimentDataType == null ? null : testExperimentDataType.getId());

                model.addAttribute("runList", exp.getIdRunList());
                return "experiment/configExperiment";
            }
        }


        exp = experimentSection(exp, user, testExperimentDataType, expDataType, configExpDto, updGrammar,
                null, null);
        // END - Experiment section

        // Create ExpPropertiesDto file
        ExpPropertiesDto propertiesDto = new ExpPropertiesDto(user, 0.0, configExpDto.getTournament(),
                0, configExpDto.getCrossoverProb(), grammarFileSection(user, configExpDto, updGrammar),
                0, 1, configExpDto.getMutationProb(), false,
                1, configExpDto.getNumCodons(), configExpDto.getPopulationSize(),
                configExpDto.getGenerations(), false, configExpDto.getMaxWraps(), 500,
                configExpDto.getExperimentName(), configExpDto.getExperimentDescription());

        removeRuns(exp);

        experimentService.saveExperiment(exp);

        modelAddData(model, user, grammarRepository.findGrammarById(Long.valueOf(grammarId)),
                experimentService.findExperimentDataTypeById(Long.valueOf(experimentDataTypeId)),
                exp.getIdExpDataTypeList(), testExperimentDataType == null ? null : testExperimentDataType.getId());
        return "experiment/configExperiment";

    }

    @RequestMapping(value = "/experiment/start", method = RequestMethod.POST, params = "cloneExperimentButton")
    public String cloneExperiment(Model model,
                                  @RequestParam("grammarId") String grammarId,
                                  @RequestParam("experimentDataTypeId") String experimentDataTypeId,
                                  @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                  @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto) throws IllegalStateException {
        User user = userService.getLoggedInUser();
        configExpDto.setId(null);
        configExpDto.setDefaultRunId(null);

        model.addAttribute("configuration", configExpDto);
        model.addAttribute("expConfig", configExpDto);
        modelAddData(model, user, grammarRepository.findGrammarById(Long.valueOf(grammarId)),
                experimentService.findExperimentDataTypeById(Long.valueOf(experimentDataTypeId)),
                null, configExpDto.getTestDefaultExpDataTypeId());
        model.addAttribute("disabledClone", true);
        model.addAttribute("messageClone", "This experiment is cloned and not saved yet.");
        return "experiment/configExperiment";
    }


    private void fileConfig(Dataset expDataType, User user, ExpPropertiesDto propertiesDto,
                            ConfigExperimentDto configExpDto,
                            Run run, Experiment exp) throws IOException {
        // Reader - FILE DATA TYPE - Convert MultipartFile into Generic Java File - Then convert it to Reader
        String dataTypeDirectoryPath = DATATYPE_DIR_PATH;
        if (expDataType.getDataTypeType().equals("validation")) {
            dataTypeDirectoryPath += "validation\\" + user.getId();
            propertiesDto.setValidationPath(dataTypeDirectoryPath);
            propertiesDto.setValidation(true);
        } else if (expDataType.getDataTypeType().equals("test")) {
            dataTypeDirectoryPath += "test\\" + user.getId();
            propertiesDto.setTestPath(dataTypeDirectoryPath);
            propertiesDto.setTest(true);
        } else {
            dataTypeDirectoryPath += "training\\" + user.getId();
            propertiesDto.setTrainingPath(dataTypeDirectoryPath + File.separator + configExpDto.getExperimentName()
                    + "_" + expDataType.getId() + ".csv");
            propertiesDto.setTraining(true);
        }

        File dir = new File(PROPERTIES_DIR_PATH + user.getId());
        if (!dir.exists())
            dir.mkdirs();

        String propertiesFilePath = PROPERTIES_DIR_PATH + user.getId() + File.separator + configExpDto.getExperimentName().replaceAll("\\s+", "") + "_" + propertiesDto.getId() + ".properties";
        createPropertiesFile(propertiesFilePath, propertiesDto, configExpDto.getExperimentName());  // Write in property file
        // END - Create ExpPropertiesDto file

        // Execute program with experiment info
        File propertiesFile = new File(propertiesFilePath);
        Reader propertiesReader = new FileReader(propertiesFile);

        Properties properties = new Properties();
        properties.load(propertiesReader);

        properties.setProperty(TRAINING_PATH_PROP, "");

        ExpProperties expPropertiesEntity = experimentService.saveExpProperties(new ExpProperties());
        createExpPropertiesEntity(expPropertiesEntity, properties, exp, run, propertiesDto);

        propertiesReader.close();
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

        Experiment exp = experimentService.findExperimentByUserIdAndExpId(user, Long.parseLong(id));

        Grammar grammar = experimentService.findGrammarById(exp.getDefaultGrammar());
        List<Run> runList = exp.getIdRunList();

        ConfigExperimentDto configExpDto = fillConfigExpDto(new ConfigExperimentDto(), exp,
                exp.getIdRunList().isEmpty() ? null : runService.findByRunId(exp.getDefaultRunId()),
                grammar);

        modelAddData(model, user, grammar, experimentService.findExperimentDataTypeById(exp.getDefaultExpDataType()),
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
                runIt.getDiagramData().setStopped(true);
                runIt.setStatus(Run.Status.STOPPED);

                Iterator<DiagramPair> it = runIt.getDiagramData().getListPair().iterator();
                ArrayList<DiagramPair> lPairAux = new ArrayList<>();
                while (it.hasNext()) {
                    DiagramPair value = it.next();
                    it.remove();
                    lPairAux.add(value);
                }
                runIt.getDiagramData().getListPair().removeAll(lPairAux);
                diagramDataService.saveDiagram(runIt.getDiagramData());
                runService.saveRun(runIt);

                th.interrupt();
                runnables.get(threadId).stopExecution();
            }
            listRunIt.remove();
            runIt.setExperimentId(null);
            runIt.getDiagramData().setRunId(null);
            runService.removeExecutionReport(runService.getRunExecutionReport(runIt.getId()));
            experimentService.deleteExpProperties(experimentService.findPropertiesById(runIt.getIdProperties()));
            diagramDataService.deleteDiagram(runIt.getDiagramData());
        }

        Iterator<Grammar> listGrammarIt = expConfig.getIdGrammarList().iterator();
        while (listGrammarIt.hasNext()) {
            listGrammarIt.next();
            listGrammarIt.remove();
        }

        Iterator<Dataset> listDataTypeIt = expConfig.getIdExpDataTypeList().iterator();
        while (listDataTypeIt.hasNext()) {
            listDataTypeIt.next();
            listDataTypeIt.remove();
        }

        expConfig.setDefaultGrammar(null);
        experimentService.saveExperiment(expConfig);
        experimentService.deleteExperiment(expConfig);
        return idExp;
    }

    private ExperimentDetailsDto setExperimentDetailDto(Run run, DiagramData diagramData) {
        ExperimentDetailsDto experimentDetailsDto = new ExperimentDetailsDto();
        experimentDetailsDto.setExperimentId(run.getExperimentId().getId());
        experimentDetailsDto.setExperimentName(run.getExperimentId().getExperimentName());
        experimentDetailsDto.setExperimentDescription(run.getExperimentId().getExperimentDescription());
        experimentDetailsDto.setRunId(run.getId());
        experimentDetailsDto.setStatus(run.getStatus());
        experimentDetailsDto.setGenerations(run.getExperimentId().getGenerations());
        experimentDetailsDto.setPopulationSize(run.getExperimentId().getPopulationSize());
        experimentDetailsDto.setMaxWraps(run.getExperimentId().getMaxWraps());
        experimentDetailsDto.setTournament(run.getExperimentId().getTournament());
        experimentDetailsDto.setCrossoverProb(run.getExperimentId().getCrossoverProb());
        experimentDetailsDto.setMutationProb(run.getExperimentId().getMutationProb());
        experimentDetailsDto.setInitialization(run.getExperimentId().getInitialization());
        experimentDetailsDto.setResults(run.getExperimentId().getResults());
        experimentDetailsDto.setNumCodons(run.getExperimentId().getNumCodons());
        experimentDetailsDto.setNumberRuns(run.getExperimentId().getNumberRuns());
        experimentDetailsDto.setDefaultGrammarId(run.getExperimentId().getDefaultGrammar());
        experimentDetailsDto.setDefaultExpDataTypeId(run.getExperimentId().getDefaultExpDataType());
        experimentDetailsDto.setIniDate(run.getIniDate().toString());
        experimentDetailsDto.setLastDate(run.getModificationDate().toString());
        if (diagramData != null) {
            experimentDetailsDto.setBestIndividual(diagramData.getBestIndividual());
            experimentDetailsDto.setCurrentGeneration(diagramData.getCurrentGeneration());
        }
        experimentDetailsDto.setExecutionReport(runService.getRunExecutionReport(run.getId()).getExecutionReport());
        return experimentDetailsDto;
    }

    @GetMapping(value = "/experiment/runList", params = "showPlotExecutionButton")
    public String showPlotExecutionExperiment(Model model,
                                              @RequestParam(value = "runId") String runId) {
        Long longRunId = Long.parseLong(runId);
        Run run = runService.findByRunId(longRunId);
        DiagramData diagramData = diagramDataService.findByRunId(run);

        ExperimentDetailsDto experimentDetailsDto = setExperimentDetailDto(run, diagramData);
        model.addAttribute("expDetails", experimentDetailsDto);

        return "experiment/experimentDetails";
    }

    @GetMapping(value = "/experiment/runList", params = "showTestStatsPlotButton")
    public String showRunTestStatsExperiment(Model model,
                                             @RequestParam(value = "runId") String runId) throws EvaluationException {
        Run run = runService.findByRunId(Long.parseLong(runId));
        List<Double> listYLine = new ArrayList<>();
        List<Double> listFunctionResult = new ArrayList<>();
        List<Double> trainingResult = new ArrayList<>();
        String[] splitContent =
                experimentService.findExperimentDataTypeById(run.getDefaultExpDataTypeId()).getInfo().split("\r\n");

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

        if (run.getExperimentId().getDefaultTestExpDataTypeId() != null) {
            List<Double> testListYLine = new ArrayList<>();
            List<Double> testListFunctionResult = new ArrayList<>();
            List<Double> testResult = new ArrayList<>();
            splitContent = experimentService.findExperimentDataTypeById(run.getExperimentId().getDefaultTestExpDataTypeId()).getInfo().split("\r\n");

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
            listYLine.add(yValue);
            listFunctionResult.add(modelValue);
            yDoubleArray[i - 1] = yValue;
            functionResultDoubleArray[i - 1] = modelValue;
        }
        // RMSE AVGERROR RSQUARE ABSOLUTEERROR
        result.add(UtilStats.computeRMSE(yDoubleArray, functionResultDoubleArray));
        result.add(UtilStats.computeAvgError(yDoubleArray, functionResultDoubleArray));
        result.add(UtilStats.computeRSquare(yDoubleArray, functionResultDoubleArray));
        result.add(UtilStats.computeAbsoluteError(yDoubleArray, functionResultDoubleArray));
    }

    private Thread runExperimentDetails(User user, Run run, DiagramData diagramData) throws IOException {
        ExpProperties prop = experimentService.findPropertiesById(run.getIdProperties());
        String propertiesFilePath = PROPERTIES_DIR_PATH + user.getId() + File.separator + run.getExperimentName().replaceAll("\\s+", "") + "_" + prop.getUuidPropDto() + ".properties";

        File propertiesFile = new File(propertiesFilePath);
        Reader propertiesReader = new FileReader(propertiesFile);

        Properties properties = new Properties();
        properties.load(propertiesReader);
        properties.setProperty(TRAINING_PATH_PROP, prop.getTrainingPath());
        RunnableExpGramEv obj = new RunnableExpGramEv(properties, diagramData, run,
                experimentService.findExperimentDataTypeById(run.getDefaultExpDataTypeId()), runService);
        Thread.UncaughtExceptionHandler h = (th, ex) -> {
            run.setStatus(Run.Status.FAILED);
            run.getDiagramData().setFailed(true);
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

    private void createPropertiesFile(String propertiesFilePath, ExpPropertiesDto propertiesDto, String expName ) throws IOException {
        File propertiesNewFile = new File(propertiesFilePath);
        if (!propertiesNewFile.exists()) {
            propertiesNewFile.createNewFile();
        }

        PrintWriter propertiesWriter = new PrintWriter(propertiesNewFile);

        propertiesWriter.println("# ExpPropertiesDto for " + expName);
        propertiesWriter.println("LoggerBasePath=" + propertiesDto.getLoggerBasePath().replace("\\", "/"));
        propertiesWriter.println("ErrorThreshold=" + propertiesDto.getErrorThreshold());
        propertiesWriter.println("TournamentSize=" + propertiesDto.getTournamentSize());
        propertiesWriter.println("WorkDir=" + propertiesDto.getWorkDir().replace("\\", "/"));
        propertiesWriter.println("RealDataCopied=" + propertiesDto.getRealDataCopied());
        propertiesWriter.println("CrossoverProb=" + propertiesDto.getCrossoverProb());
        propertiesWriter.println("BnfPathFile=" + propertiesDto.getBnfPathFile().substring(2).replace("\\", "/"));
        propertiesWriter.println("Objectives=" + propertiesDto.getObjectives());
        propertiesWriter.println("ClassPathSeparator=" + propertiesDto.getClassPathSeparator());
        propertiesWriter.println("Executions=" + propertiesDto.getExecutions());
        propertiesWriter.println("LoggerLevel=" + propertiesDto.getLoggerLevel());
        propertiesWriter.println("MutationProb=" + propertiesDto.getMutationProb());
        propertiesWriter.println("NormalizeData=" + propertiesDto.getNormalizedData());
        propertiesWriter.println("LogPopulation=" + propertiesDto.getLogPopulation());
        propertiesWriter.println("ChromosomeLength=" + propertiesDto.getChromosomeLength());
        propertiesWriter.println("NumIndividuals=" + propertiesDto.getNumIndividuals());
        propertiesWriter.println("NumGenerations=" + propertiesDto.getNumGenerations());
        propertiesWriter.println("ViewResults=" + propertiesDto.getViewResults());
        propertiesWriter.println("MaxWraps=" + propertiesDto.getMaxWraps());
        propertiesWriter.println("ModelWidth=" + propertiesDto.getModelWidth());

        if (propertiesDto.getTraining())
            propertiesWriter.println("TrainingPath=" + propertiesDto.getTrainingPath().substring(2).replace("\\", "/"));
        else if (propertiesDto.getValidation()) {
            propertiesWriter.println("TrainingPath=" + propertiesDto.getTrainingPath().substring(2).replace("\\", "/")); // TEMPORAL UNTIL KNOW IF WE NEED THIS OR NOT
            propertiesWriter.println("ValidationPath=" + propertiesDto.getValidationPath().substring(2).replace("\\", "/"));
        } else {
            propertiesWriter.println("TrainingPath=" + propertiesDto.getTrainingPath().substring(2).replace("\\", "/"));
            propertiesWriter.println("TestPath=" + propertiesDto.getTestPath().substring(2).replace("\\", "/"));
            // ("\\", "/")
        }
        propertiesWriter.close();
    }

    private void createExpPropertiesEntity(ExpProperties expProp, Properties properties,
                                           Experiment experiment,
                                           Run run, ExpPropertiesDto propDto) {
        expProp.setUuidPropDto(propDto.getId().toString());

        expProp.setIdExp(experiment.getId());
        if (run != null) {
            expProp.setIdRun(run.getId());
            run.setIdProperties(expProp.getId());
        }


        expProp.setLoggerBasePath(properties.getProperty("LoggerBasePath"));
        expProp.setErrorThreshold(Double.parseDouble(properties.getProperty("ErrorThreshold")));
        expProp.setTournamentSize(Integer.parseInt(properties.getProperty("TournamentSize")));
        expProp.setWorkDir(properties.getProperty("WorkDir"));
        expProp.setRealDataCopied(Integer.parseInt(properties.getProperty("RealDataCopied")));
        expProp.setCrossoverProb(Double.parseDouble(properties.getProperty("CrossoverProb")));
        expProp.setBnfPathFile(properties.getProperty("BnfPathFile"));
        expProp.setObjectives(Integer.parseInt(properties.getProperty("Objectives")));
        expProp.setClassPathSeparator(properties.getProperty("ClassPathSeparator"));
        expProp.setExecutions(Integer.parseInt(properties.getProperty("Executions")));
        expProp.setLoggerLevel(properties.getProperty("LoggerLevel"));
        expProp.setMutationProb(Double.parseDouble(properties.getProperty("MutationProb")));
        expProp.setNormalizedData(Boolean.parseBoolean(properties.getProperty("NormalizeData")));
        expProp.setLogPopulation(Integer.parseInt(properties.getProperty("LogPopulation")));
        expProp.setChromosomeLength(Integer.parseInt(properties.getProperty("ChromosomeLength")));
        expProp.setNumIndividuals(Integer.parseInt(properties.getProperty("NumIndividuals")));
        expProp.setNumGenerations(Integer.parseInt(properties.getProperty("NumGenerations")));
        expProp.setViewResults(Boolean.parseBoolean(properties.getProperty("ViewResults")));
        expProp.setMaxWraps(Integer.parseInt(properties.getProperty("MaxWraps")));
        expProp.setModelWidth(Integer.parseInt(properties.getProperty("ModelWidth")));

        expProp.setTrainingPath("");

        expProp.setExperimentName(experiment.getExperimentName());
        expProp.setExperimentDescription(experiment.getExperimentDescription());
        expProp.setInitialization(experiment.getInitialization());
        expProp.setResults(experiment.getResults());
        expProp.setNumberRuns(experiment.getNumberRuns());

    }

    private void runSection(Run run, Grammar grammar, ConfigExperimentDto configExpDto) {
        run.setDefaultRunId(run.getId());
        run.setStatus(Run.Status.INITIALIZING);

        run.setIniDate(new Timestamp(System.currentTimeMillis()));
        run.setModificationDate(new Timestamp(System.currentTimeMillis()));

        run.setExperimentName(configExpDto.getExperimentName());
        run.setExperimentDescription(configExpDto.getExperimentDescription());

        run.setDefaultGrammarId(grammar.getId());
        run.setGenerations(configExpDto.getGenerations());
        run.setPopulationSize(configExpDto.getPopulationSize());
        run.setMaxWraps(configExpDto.getMaxWraps());
        run.setTournament(configExpDto.getTournament());
        run.setCrossoverProb(configExpDto.getCrossoverProb());
        run.setMutationProb(configExpDto.getMutationProb());
        run.setInitialization(configExpDto.getInitialization());
        run.setObjective(configExpDto.getObjective());
        run.setResults(configExpDto.getResults());
        run.setNumCodons(configExpDto.getNumCodons());
        run.setNumberRuns(configExpDto.getNumberRuns());
        RunExecutionReport runExecutionReport = new RunExecutionReport();
        runExecutionReport.setId(run.getId());
        runService.saveRunExecutionReport(runExecutionReport);

    }

    private String grammarFileSection(User user, ConfigExperimentDto configExpDto, Grammar grammar) throws IllegalStateException, IOException {

        File dir = new File(GRAMMAR_DIR_PATH + user.getId());
        if (!dir.exists())
            dir.mkdirs();

        String grammarFilePath = GRAMMAR_DIR_PATH + user.getId() + File.separator + configExpDto.getExperimentName().replaceAll("\\s+", "") + "_" + grammar.getId() + ".bnf";

        File grammarNewFile = new File(grammarFilePath);
        if (!grammarNewFile.exists()) {
            grammarNewFile.createNewFile();
        }

        PrintWriter grammarWriter = new PrintWriter(grammarNewFile);

        String[] parts = grammar.getFileText().split("\\r\\n");
        for (String part : parts) {
            grammarWriter.println(part);
        }

        grammarWriter.close();

        return grammarFilePath;
    }

    private void experimentDataTypeSection(Dataset expDataType) {
        expDataType.setCreationDate(new Timestamp(System.currentTimeMillis()));
        expDataType.setDataTypeType("training");
    }

    private Experiment experimentSection(Experiment exp, User user, Dataset testExpDataType,
                                         Dataset expDataType,
                                         ConfigExperimentDto configExpDto, Grammar grammar, Run run,
                                         Long longDefaultRunId) {
        if (exp == null) {   // We create it
            exp = new Experiment(user, configExpDto.getExperimentName(), configExpDto.getExperimentDescription(), configExpDto.getGenerations(),
                    configExpDto.getPopulationSize(), configExpDto.getMaxWraps(), configExpDto.getTournament(), configExpDto.getCrossoverProb(), configExpDto.getMutationProb(),
                    configExpDto.getInitialization(), configExpDto.getResults(), configExpDto.getNumCodons(), configExpDto.getNumberRuns(), configExpDto.getObjective(),
                    new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
            if (longDefaultRunId != null) {
                exp.setDefaultRunId(longDefaultRunId);          // Doesn't exists -> We set up the run id obtained before
            }
            user.addExperiment(exp);       // We add it only if doesn't exist

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
            exp.setInitialization(configExpDto.getInitialization());
            exp.setResults(configExpDto.getResults());
            exp.setNumCodons(configExpDto.getNumCodons());
            exp.setNumberRuns(configExpDto.getNumberRuns());
            exp.setObjective(configExpDto.getObjective());

            exp.setCreationDate(new Timestamp(System.currentTimeMillis()));
            exp.setModificationDate(new Timestamp(System.currentTimeMillis()));

            removeRuns(exp);

            if (run != null) {
                exp.setDefaultRunId(run.getId());          // Doesn't exists -> We set up the run id obtained before
            }
        }
        if (run != null) {
            exp.addRun(run);
        }
        exp.addGrammar(grammar);
        exp.addExperimentDataType(expDataType);
        exp.setDefaultGrammar(grammar.getId());
        exp.setDefaultExpDataType(expDataType.getId());
        exp.setDefaultTestExpDataTypeId(testExpDataType != null ? testExpDataType.getId() : null);

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
                                    RedirectAttributes redirectAttrs) {
        Run run = runService.findByRunId(Long.parseLong(runIdStop));
        Long threadId = run.getThreaId();

        // https://stackoverflow.com/questions/26213615/terminating-thread-using-thread-id-in-java
        Thread th = threadMap.get(threadId);
        if (th == null) {
            run.setStatus(Run.Status.FAILED);
            run.getDiagramData().setFailed(true);

            redirectAttrs.addAttribute("runId", run.getId()).addFlashAttribute("Stop", "Stop execution failed");
            redirectAttrs.addAttribute("showPlotExecutionButton", "showPlotExecutionButton");
            return "redirect:experiment/runList";
        }
        th.interrupt();
        runnables.get(threadId).stopExecution();

        run.setStatus(Run.Status.STOPPED);
        run.getDiagramData().setStopped(true);
        diagramDataService.saveDiagram(run.getDiagramData());
        runService.saveRun(run);

        ExperimentDetailsDto experimentDetailsDto = setExperimentDetailDto(run, run.getDiagramData());

        model.addAttribute("expDetails", experimentDetailsDto);
        return "experiment/experimentDetails";
    }

    @RequestMapping(value = "/experiment/expRepoSelected", method = RequestMethod.POST, params = "deleteRun")
    public
    @ResponseBody
    Long deleteRun(@RequestParam("runId") String runId) {
        boolean found = false;

        Long longRunId = Long.parseLong(runId);
        Run run = runService.findByRunId(longRunId);
        Experiment experiment = run.getExperimentId();

        List<Grammar> lGrammar = experiment.getIdGrammarList();
        Iterator<Grammar> grammarIt = lGrammar.iterator();
        while (grammarIt.hasNext() && !found) {
            Grammar grammarAux = grammarIt.next();
            if (grammarAux.getId().longValue() == run.getDefaultGrammarId().longValue()) {

                if (grammarAux.getId().longValue() == experiment.getDefaultGrammar().longValue())
                    experiment.setDefaultRunId(Long.parseLong("0"));
                found = true;
            }
        }
        found = false;

        List<Run> lRun = run.getExperimentId().getIdRunList();
        Iterator<Run> runIt = lRun.iterator();
        while (runIt.hasNext() && !found) {
            Run runAux = runIt.next();
            if (runAux.getId().longValue() == run.getId().longValue()) {
                runAux.getDiagramData().setRunId(null);
                diagramDataService.deleteDiagram(runAux.getDiagramData());
                runAux.setExperimentId(null);
                if (runAux.getId().longValue() == experiment.getDefaultRunId().longValue())
                    experiment.setDefaultRunId(Long.parseLong("0"));
                found = true;
            }
        }

        if (experimentService.findPropertiesById(run.getIdProperties()) != null)
            runService.deleteExpProperties(experimentService.findPropertiesById(run.getIdProperties()));
        if (experiment.getDefaultRunId().equals(longRunId)) {
            experiment.setDefaultRunId(null);
            experimentService.saveExperiment(experiment);
        }
        return longRunId;
    }

    public ConfigExperimentDto fillConfigExpDto(ConfigExperimentDto configExpDto, Experiment exp, Run run, Grammar grammar) {
        if (run == null) {
            configExpDto.setDefaultRunId(exp.getDefaultRunId());
            setConfigExpDtoWIthExperiment(configExpDto, exp.getExperimentName(),
                    exp.getExperimentDescription(), exp.getCrossoverProb(), exp.getGenerations(),
                    exp.getPopulationSize(), exp.getMaxWraps(), exp.getTournament(), exp.getMutationProb(),
                    exp.getInitialization(), exp.getResults(), exp.getNumCodons(), exp.getNumberRuns(), exp.getObjective(), exp);
        } else {
            configExpDto.setDefaultRunId(run.getId());
            setConfigExpDtoWIthExperiment(configExpDto, run.getExperimentName(),
                    run.getExperimentDescription(), run.getCrossoverProb(), run.getGenerations(),
                    run.getPopulationSize(), run.getMaxWraps(), run.getTournament(),
                    run.getMutationProb(), run.getInitialization(), run.getResults(), run.getNumCodons(),
                    run.getNumberRuns(), run.getObjective(), exp);
        }

        configExpDto.setId(exp.getId());
        configExpDto.setDefaultExpDataTypeId(exp.getDefaultExpDataType());
        configExpDto.setDefaultGrammarId(exp.getDefaultGrammar());

        configExpDto.setGrammarName(grammar.getGrammarName());
        configExpDto.setGrammarDescription(grammar.getGrammarDescription());
        configExpDto.setFileText(grammar.getFileText());


        return configExpDto;
    }

    private void setConfigExpDtoWIthExperiment(ConfigExperimentDto configExpDto, String experimentName, String experimentDescription, Double crossoverProb, Integer generations, Integer populationSize, Integer maxWraps, Integer tournament, Double mutationProb, String initialization, String results, Integer numCodons, Integer numberRuns, String objective, Experiment exp) {
        configExpDto.setExperimentName(experimentName);
        configExpDto.setExperimentDescription(experimentDescription);
        configExpDto.setCrossoverProb(crossoverProb);
        configExpDto.setGenerations(generations);
        configExpDto.setPopulationSize(populationSize);
        configExpDto.setMaxWraps(maxWraps);
        configExpDto.setTournament(tournament);
        configExpDto.setCrossoverProb(crossoverProb);
        configExpDto.setMutationProb(mutationProb);
        configExpDto.setInitialization(initialization);
        configExpDto.setResults(results);
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
                        infoFormated += matcher.group()+"\r\n";
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

}