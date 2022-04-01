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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@Controller
public class ExperimentController {
    private static final String CONFIGEXPERIMENTPATH = "experiment/configExperiment";
    Logger logger = Logger.getLogger(ExperimentController.class.getName());

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
        legacyExperimentRunnerService.modelAddDataService(model, user, null, null, null);

        return CONFIGEXPERIMENTPATH;
    }

    /**
     * Load all the data from the view, save it and run the application.
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

        return legacyExperimentRunnerService.runExperimentService(model, experimentDataTypeId, testExperimentDataTypeId, fileModelDto, configExpDto, result, redirectAttrs);
    }

    @PostMapping(value = "/experiment/start", params = "saveExperimentButton")
    public String saveExperiment(Model model,
                                 @RequestParam("experimentDataTypeId") String experimentDataTypeId,
                                 @RequestParam("testExperimentDataTypeId") String testExperimentDataTypeId,
                                 @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                 @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto,
                                 BindingResult result) {

        return legacyExperimentRunnerService.saveExperimentService(model, experimentDataTypeId, testExperimentDataTypeId
                , fileModelDto, configExpDto, result);
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

        legacyExperimentRunnerService.modelAddDataService(model, user,
                experimentService.findExperimentDataTypeById(Long.valueOf(experimentDataTypeId)),
                null, configExpDto.getTestDefaultExpDataTypeId());
        model.addAttribute("disabledClone", true);
        model.addAttribute("messageClone", "This experiment is cloned and not saved yet.");

        return CONFIGEXPERIMENTPATH;
    }

    @GetMapping(value = "/experiment/experimentRepository")
    public String experimentRepository(Model model) {
        return legacyExperimentRunnerService.experimentRepositoryService(model);
    }

    @GetMapping(value = "/experiment/expRepoSelected", params = "loadExperimentButton")
    public String expRepoSelected(Model model,
                                  @RequestParam(required = false) String id) { // Exp ID

        return legacyExperimentRunnerService.expRepoSelectedService(model, id);
    }

    @PostMapping(value = "/experiment/expRepoSelected", params = "deleteExperiment")
    public
    @ResponseBody
    Long expRepoSelectedDelete(@RequestParam("experimentId") String experimentId) {
        return legacyExperimentRunnerService.expRepoSelectedDeleteService(experimentId);
    }

    @PostMapping(value = "/experiment/expRepoSelected", params = "checkIfRunning")
    public
    @ResponseBody
    Boolean expRepoSelectedCheckRunning(@RequestParam("experimentId") String experimentId) {
        return legacyExperimentRunnerService.expRepoSelectedCheckRunningService(experimentId);
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
        return legacyExperimentRunnerService.showRunTestStatsExperimentService(model, runId);
    }

    @PostMapping(value = "/experiment/stopRun", params = "stopRunExperimentButton")
    public String stopRunExperiment(Model model,
                                    @RequestParam("runIdStop") String runIdStop,
                                    RedirectAttributes redirectAttrs) throws InterruptedException {
        return legacyExperimentRunnerService.stopRunExperimentService(model, runIdStop, redirectAttrs, diagramDataService);
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
        logger.log(Level.SEVERE,"EJECUION CANCELADA");
        logger.log(Level.SEVERE,legacyExperimentRunnerService.getExecutionCancelled()+"");
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
        return legacyExperimentRunnerService.getRunResultsInfoService(expId);
    }

    /**
     * Returns info to be downloaded in a datafile
     * @param expId
     * @return
     */
    @GetMapping(value = "/experimentRunsPredictions")
    @ResponseBody
    public Map<String,String[][]> getExperimentPredictions(@RequestParam("expId") String expId) {
        return legacyExperimentRunnerService.getExperimentPredictionsService(expId);
    }
}