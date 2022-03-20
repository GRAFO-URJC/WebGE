package com.gramevapp.web.controller;

import com.gramevapp.web.model.*;
import com.gramevapp.web.repository.GrammarRepository;
import com.gramevapp.web.service.*;
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

import static com.engine.util.Common.TRAINING_PATH_PROP;

@Controller
public class ExperimentController {
    private static final String CONFIGEXPERIMENTPATH = "experiment/configExperiment";

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

    @Autowired
    private SaveDBService saveDBService;

    @Autowired
    private LegacyExperimentRunnerService legacyExperimentRunnerService;


    @ModelAttribute
    public FileModelDto fileModel() {
        return new FileModelDto();
    }

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

    @GetMapping("/experiment/configExperiment")
    public String configExperiment(Model model,
                                   @ModelAttribute("configuration") ConfigExperimentDto configExpDto) {
        User user = userService.getLoggedInUser();

        model.addAttribute("type", new Dataset());
        model.addAttribute(CONFIGURATION, configExpDto);
        model.addAttribute(RUNLIST, null);
        model.addAttribute("user", user);
        model.addAttribute("configExp", new ConfigExperimentDto());
        model.addAttribute("disabledClone", true);
        legacyExperimentRunnerService.modelAddData(model, user, null, null, null, grammarRepository);

        return CONFIGEXPERIMENTPATH;
    }

    /**
     * Load al the data from the view, save it and run the application.
     * "configExpDto" for validation -> configExp
     * "configuration" is for send data from Controller to View and
     * "configExp" is the object from the form View
     */
    @PostMapping(value = "/experiment/start", params = "runExperimentButton")
    public String runExperiment(Model model,
                                @RequestParam("experimentDataTypeId") String experimentDataTypeId,
                                @RequestParam("testExperimentDataTypeId") String testExperimentDataTypeId,
                                @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto,
                                BindingResult result,
                                RedirectAttributes redirectAttrs) throws IOException {

        User user = userService.getLoggedInUser();
        legacyExperimentRunnerService.modelAddData(model, user, null, null, null, grammarRepository);

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
        Experiment exp = legacyExperimentRunnerService.experimentSection(configExpDto.getId() != null ?
                        experimentService.findExperimentById(configExpDto.getId()) : null
                , user,
                (testExperimentDataTypeId.equals("")) ? null : experimentService.
                        findExperimentDataTypeById(Long.valueOf(testExperimentDataTypeId))
                , expDataType, configExpDto, configExpDto.getFileText(), true);

        experimentService.saveExperiment(exp);
        // END - Experiment section

        // Grammar File SECTION
        String grammarFilePath = legacyExperimentRunnerService.grammarFileSection(user, configExpDto, exp.getDefaultGrammar(), grammarRepository);
        // END - Grammar File SECTION

        Run run;
        String propPath;

        //check if need to run more runs
        for (int i = 0; i < configExpDto.getNumberRuns(); i++) {
            // RUN SECTION
            run = runService.saveRun(new Run());
            //runSection(run, exp);
            legacyExperimentRunnerService.runSection(run, exp);
            run.setStatus(Run.Status.WAITING);
            // Create ExpPropertiesDto file
            propPath = expPropertiesSet(configExpDto,
                    user, expDataType, grammarFilePath);
            // Run experiment in new thread
            int crossRunIdentifier = exp.isCrossExperiment() ? run.getExperimentId().getIdRunList().indexOf(run) + 1 : -1;

            legacyExperimentRunnerService.accept(run, propPath, crossRunIdentifier, configExpDto.getObjective(), configExpDto.isDe());

        }
        experimentService.saveExperiment(exp);
        legacyExperimentRunnerService.setExecutionCancelled(false);

        // start experiment
        legacyExperimentRunnerService.startExperiment();

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

        // Write in property file
        legacyExperimentRunnerService.createPropertiesFile(propertiesFilePath, configExpDto.getExperimentName(),
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


    @PostMapping(value = "/experiment/start", params = "saveExperimentButton")
    public String saveExperiment(Model model,
                                 @RequestParam("experimentDataTypeId") String experimentDataTypeId,
                                 @RequestParam("testExperimentDataTypeId") String testExperimentDataTypeId,
                                 @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                 @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto,
                                 BindingResult result) {

        User user = userService.getLoggedInUser();
        legacyExperimentRunnerService.modelAddData(model, user, null, null, null, grammarRepository);
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
           /* configExpDto = fillConfigExpDto(configExpDto, exp,
            exp.getDefaultGrammar(), expDataType, true);
            // check if only test was changed

            boolean sameExp =
                    exp.getGenerations().equals(configExpDto.getGenerations()) &&
                            exp.getCrossoverProb().equals(configExpDto.getCrossoverProb()) &&
                            exp.getPopulationSize().equals(configExpDto.getPopulationSize()) &&
                            exp.getMutationProb().equals(configExpDto.getMutationProb()) &&

                            exp.getMaxWraps().equals(configExpDto.getMaxWraps()) &&
                            exp.getTournament().equals(configExpDto.getTournament()) &&
                            exp.getNumberRuns().equals(configExpDto.getNumberRuns()) &&
                            exp.getObjective().equals(configExpDto.getObjective()) &&
                            exp.getDefaultGrammar().equals(configExpDto.getFileText()) &&
                            exp.getDefaultExpDataType().equals(Long.valueOf(experimentDataTypeId)) &&
                            exp.getTags().equals(configExpDto.getTagsText());
            if (sameExp) {
                if (!testExperimentDataTypeId.equals("")) {
                    exp.setDefaultTestExpDataTypeId(Long.valueOf(testExperimentDataTypeId));
                } else {
                    exp.setDefaultTestExpDataTypeId(null);
                }
                exp.setModificationDate(new Timestamp(new Date().getTime()));
                exp.setExperimentName(configExpDto.getExperimentName());
                exp.setExperimentDescription(configExpDto.getExperimentDescription());
                experimentService.saveExperiment(exp);
                modelAddData(model, user,
                        experimentService.findExperimentDataTypeById(Long.valueOf(experimentDataTypeId)),
                        exp.getIdExpDataTypeList(), testExperimentDataType == null ? null : testExperimentDataType.getId());

                model.addAttribute(RUNLIST, exp.getIdRunList());
                return CONFIGEXPERIMENTPATH;
            }
            */

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
        exp = legacyExperimentRunnerService.experimentSection(exp, user, testExperimentDataType, expDataType, configExpDto, configExpDto.getFileText(), !sameExp);

        List<Run> runList = exp.getIdRunList();

        experimentService.saveExperiment(exp);
        legacyExperimentRunnerService.fillConfigExpDto(configExpDto, exp, exp.getDefaultGrammar(), expDataType, false);

        legacyExperimentRunnerService.modelAddData(model, user,
                experimentService.findExperimentDataTypeById(Long.valueOf(experimentDataTypeId)),
                exp.getIdExpDataTypeList(), testExperimentDataType == null ? null : testExperimentDataType.getId(), grammarRepository);

        model.addAttribute(EXPCONFIG, configExpDto);
        model.addAttribute(RUNLIST, runList);
        return CONFIGEXPERIMENTPATH;

    }

    @PostMapping(value = "/experiment/start", params = "cloneExperimentButton")
    public String cloneExperiment(Model model,
                                  @RequestParam("experimentDataTypeId") String experimentDataTypeId,
                                  @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                  @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto) {
        User user = userService.getLoggedInUser();
        configExpDto.setId(null);

        model.addAttribute(CONFIGURATION, configExpDto);
        model.addAttribute(EXPCONFIG, configExpDto);

        legacyExperimentRunnerService.modelAddData(model, user,
                experimentService.findExperimentDataTypeById(Long.valueOf(experimentDataTypeId)),
                null, configExpDto.getTestDefaultExpDataTypeId() ,grammarRepository);
        model.addAttribute("disabledClone", true);
        model.addAttribute("messageClone", "This experiment is cloned and not saved yet.");

        return CONFIGEXPERIMENTPATH;
    }

    @GetMapping(value = "/experiment/experimentRepository")
    public String experimentRepository(Model model) {

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

    @GetMapping(value = "/experiment/expRepoSelected", params = "loadExperimentButton")
    public String expRepoSelected(Model model,
                                  @RequestParam(required = false) String id) { // Exp ID

        User user = userService.getLoggedInUser();

        if (id == null)
            return "redirect:experiment/experimentRepository";

        Experiment exp = experimentService.findExperimentById(Long.parseLong(id));

        List<Run> runList = exp.getIdRunList();

        ConfigExperimentDto configExpDto = legacyExperimentRunnerService.fillConfigExpDto(new ConfigExperimentDto(), exp,
                exp.getDefaultGrammar(), experimentService.findExperimentDataTypeById(exp.getDefaultExpDataType()),
                false);

        legacyExperimentRunnerService.modelAddData(model, user, experimentService.findExperimentDataTypeById(exp.getDefaultExpDataType()),
                exp.getIdExpDataTypeList(), exp.getDefaultTestExpDataTypeId(), grammarRepository);
        model.addAttribute(CONFIGURATION, configExpDto);
        model.addAttribute("configExp", configExpDto);
        model.addAttribute(RUNLIST, runList);

        return CONFIGEXPERIMENTPATH;
    }

    @PostMapping(value = "/experiment/expRepoSelected", params = "deleteExperiment")
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
            Thread th = legacyExperimentRunnerService.getThreadMap().get(threadId);
            if (th != null) {
                runIt.setStatus(Run.Status.STOPPED);
                runService.saveRun(runIt);
                th.interrupt();
                legacyExperimentRunnerService.getRunnables().get(threadId).stopExecution(); //TODO
            }
            listRunIt.remove();
            runIt.setExperimentId(null);
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


    @PostMapping(value = "/experiment/expRepoSelected", params = "checkIfRunning")
    public
    @ResponseBody
    Boolean expRepoSelectedCheckRunning(@RequestParam("experimentId") String experimentId) {
        Long idExp = Long.parseLong(experimentId);

        Experiment expConfig = experimentService.findExperimentById(idExp);

        Iterator<Run> listRunIt = expConfig.getIdRunList().iterator();
        while (listRunIt.hasNext()) {
            Run runIt = listRunIt.next();

            if (runIt.getStatus().equals(Run.Status.RUNNING)) {
                return true;
            }

        }
        return false;
    }


    @GetMapping(value = "/experiment/runList", params = "showPlotExecutionButton")
    public String showPlotExecutionExperiment(Model model,
                                              @RequestParam(value = RUNID) String runId) {
        Long longRunId = Long.parseLong(runId);
        Run run = runService.findByRunId(longRunId);

        model.addAttribute(EXPDETAILS, run.getExperimentId());
        model.addAttribute(RUNID, run.getId());
        model.addAttribute(RUN, run);
        model.addAttribute(INDEX, run.getExperimentId().getIdRunList().indexOf(run) + 1);

        return "experiment/experimentDetails";
    }

    @GetMapping(value = "/experiment/runList", params = "showTestStatsPlotButton")
    public String showRunTestStatsExperiment(Model model,
                                             @RequestParam(value = RUNID) String runId) {
        Run run = runService.findByRunId(Long.parseLong(runId));

        HashMap<String, List<Double>> results = legacyExperimentRunnerService.collectTrainingAndTestStats(run,true);


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

    private void experimentDataTypeSection(Dataset expDataType) {
        expDataType.setCreationDate(new Timestamp(new Date().getTime()));
        expDataType.setDataTypeType("training");
    }

    @PostMapping(value = "/experiment/stopRun", params = "stopRunExperimentButton")
    public String stopRunExperiment(Model model,
                                    @RequestParam("runIdStop") String runIdStop,
                                    RedirectAttributes redirectAttrs) throws InterruptedException {
        Run run = runService.findByRunId(Long.parseLong(runIdStop));
        Long threadId = run.getThreaId();

        // https://stackoverflow.com/questions/26213615/terminating-thread-using-thread-id-in-java
        Thread th = legacyExperimentRunnerService.getThreadMap().get(threadId);
        if (th == null) {
            run.setStatus(Run.Status.FAILED);

            if (redirectAttrs != null) {
                redirectAttrs.addAttribute(RUNID, run.getId()).addFlashAttribute("Stop", "Stop execution failed");
                redirectAttrs.addAttribute("showPlotExecutionButton", "showPlotExecutionButton");
            }
            return "redirect:experiment/runList";
        }
        th.interrupt();
        legacyExperimentRunnerService.getRunnables().get(threadId).stopExecution();
        th.join();
        run = runService.findByRunId(Long.parseLong(runIdStop));
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

    @PostMapping(value = "/experiment/stopRunAjax")
    @ResponseBody
    public boolean ajaxStopRunExperiment(@RequestParam("runIdStop") String runIdStop) throws InterruptedException {
        this.stopRunExperiment(null, runIdStop, null);
        return true;
    }

    @PostMapping(value = "/experiment/stopAllRunsAjax")
    @ResponseBody
    public boolean ajaxStopAllRunsExperiment(@RequestParam("expId") Long expId) throws InterruptedException {
        legacyExperimentRunnerService.setExecutionCancelled(true);
        Experiment experiment = experimentService.findExperimentById(expId);
        List<Run> runList = experiment.getIdRunList();
        for (Run run : runList) {

            if (!run.getStatus().equals(Run.Status.RUNNING) && !run.getStatus().equals(Run.Status.FINISHED)) {
                run.setStatus(Run.Status.CANCELLED);
                saveDBService.saveRunAsync(run);
            } else if (!run.getStatus().equals(Run.Status.FINISHED)) {
                ajaxStopRunExperiment(String.valueOf(run.getId()));
            }

        }
        return true;
    }


    @PostMapping(value = "/experiment/expRepoSelected", params = "deleteRun")
    public
    @ResponseBody
    Long deleteRun(@RequestParam(RUNID) String runId) {
        Long longRunId = Long.parseLong(runId);
        Run run = runService.findByRunId(longRunId);
        runService.deleteRun(run);
        return longRunId;
    }

    @PostConstruct
    public void initSystemStream() {
        legacyExperimentRunnerService.initSystemStream();
    }

    /**
     * Returns info to be downloaded in a datafile
     * @param expId
     * @return
     */
    @GetMapping(value = "/runResultsInfo")
    @ResponseBody
    public RunResultsDto getRunResultsInfo(@RequestParam("expId") String expId) {
        Experiment experiment = experimentService.findExperimentById(Long.valueOf(expId));
        boolean haveTest = experiment.getDefaultTestExpDataTypeId() != null;
        RunResultsDto runResultsDto = new RunResultsDto(experiment.getIdRunList().size(),
                haveTest || experiment.isCrossExperiment());
        int index = 0;
        for (Run run : experiment.getIdRunList()) {

            runResultsDto.getRunIndex()[index] = index + 1;
            runResultsDto.getModel()[index] = run.getModel();

           // HashMap<String, List<Double>> results = collectTrainingAndTestStats(run,true);
            HashMap<String, List<Double>> results = legacyExperimentRunnerService.collectTrainingAndTestStats(run,true);


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

    /**
     * Returns info to be downloaded in a datafile
     * @param expId
     * @return
     */
    @GetMapping(value = "/experimentRunsPredictions")
    @ResponseBody
    public Map<String,String[][]> getExperimentPredictions(@RequestParam("expId") String expId) {
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

            //HashMap<String, List<Double>> results = collectTrainingAndTestStats(run,false);
            HashMap<String, List<Double>> results = legacyExperimentRunnerService.collectTrainingAndTestStats(run,false);

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
        String[][] trainingResults = fillInResultsAndStats("Training",trainingTarget,trainingPreds,trainingStats,models);
        finalResults.put("training",trainingResults);

        if (haveTest) {
            String[][] testResults = fillInResultsAndStats("Test",testTarget,testPreds,testStats,models);
            finalResults.put("test", testResults);
        }

        return finalResults;
    }

    private String[][] fillInResultsAndStats(String label,List<Double> target, List<List<Double>> predictions, List<List<Double>> stats, ArrayList<String> models) {
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


}