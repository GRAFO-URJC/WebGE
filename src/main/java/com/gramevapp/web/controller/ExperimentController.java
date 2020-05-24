package com.gramevapp.web.controller;

import com.engine.algorithm.RunnableExpGramEv;
import com.gramevapp.web.model.*;
import com.gramevapp.web.repository.GrammarRepository;
import com.gramevapp.web.service.DiagramDataService;
import com.gramevapp.web.service.ExperimentService;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.jws.soap.SOAPBinding;
import javax.validation.Valid;
import java.io.*;
import java.sql.Timestamp;
import java.util.*;

import static com.engine.util.Common.TRAINING_PATH_PROP;

@Controller
public class ExperimentController {

    private HashMap<Long, Thread> threadMap = new HashMap();
    private static HashMap<Long, RunnableExpGramEv> runnables = new HashMap();

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

    private void modelAddData(Model model, User user, Grammar grammar, ExperimentDataType experimentDataType,
                              List<ExperimentDataType> experimentDataTypeList) {
        model.addAttribute("grammarList", grammarRepository.findByUserId(user.getId()));
        model.addAttribute("datasetList", experimentService.findAllExperimentDataTypeByUserId(user.getId()));
        model.addAttribute("grammar", grammar);
        model.addAttribute("experimentDataType", experimentDataType);
        model.addAttribute("dataTypeList", experimentDataTypeList);
    }

    @GetMapping("/experiment/configExperiment")
    public String configExperiment(Model model,
                                   @ModelAttribute("configuration") ConfigExperimentDto configExpDto) {
        /* WE NEED TO ADD HERE THE EXPERIMENT INFO TO SEND IT TO configExperiment
         deleted code that seems to be useless, if need something go back and find
         in the commit
         https://github.com/GRAFO-URJC/WebGE/commit/2475b17f685f1e65c9c2199638410ff925613166
        */

        User user = userService.getLoggedInUser();

        model.addAttribute("type", new ExperimentDataType());
        model.addAttribute("configuration", configExpDto);
        model.addAttribute("runList", new ArrayList());
        model.addAttribute("dataTypeList", new ArrayList());
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
     *
     * @param model
     * @param grammarDto
     * @param expDataTypeDto
     * @param radioDataTypeHidden
     * @param fileModelDto
     * @param configExpDto
     * @param result
     * @param redirectAttrs
     * @return
     * @throws IllegalStateException
     * @throws IOException
     */
    @RequestMapping(value = "/experiment/start", method = RequestMethod.POST, params = "runExperimentButton")
    public String runExperiment(Model model,
                                @RequestParam("grammarId") String grammarId,
                                @RequestParam("experimentDataTypeId") String experimentDataTypeId,
                                @ModelAttribute("type") ExperimentDataTypeDto expDataTypeDto,
                                @RequestParam("radioDataType") String radioDataTypeHidden,
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

        if (radioDataTypeHidden.equals("on") && fileModelDto.getTypeFile().isEmpty()) {        // Radio button neither file path selected
            result.rejectValue("typeFile", "error.typeFile", "Choose one file");
            return "experiment/configExperiment";
        }

        if (radioDataTypeHidden.equals("on") && fileModelDto.getTypeFile().isEmpty()) {        // Radio button neither file path selected
            result.rejectValue("typeFile", "error.typeFile", "Choose one file");
            return "experiment/configExperiment";
        }


        // CONFIGURATION SECTION
        // GRAMMAR SECTION
        Grammar grammar = grammarRepository.findGrammarById(Long.parseLong(grammarId));

        // DATE TIMESTAMP
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());

        // RUN SECTION
        Run run = runService.saveRun(new Run());
        runSection(run, grammar, configExpDto, currentTimestamp);

        // Experiment Data Type SECTION
        ExperimentDataType expDataType = experimentService.
                findExperimentDataTypeById(Long.valueOf(experimentDataTypeId));

        expDataType = experimentDataTypeSection(fileModelDto, expDataType, expDataTypeDto, currentTimestamp);
        expDataType.setRunId(run.getId());
        run.setDefaultExpDataTypeId(expDataType.getId());
        // END - Experiment Data Type SECTION

        // Experiment section:
        Experiment exp = experimentSection(configExpDto.getId() != null ?
                        experimentService.findExperimentById(configExpDto.getId()) : null
                , user, expDataType, configExpDto, grammar, run, currentTimestamp, run.getId());
        exp.setDefaultGrammar(grammar.getId());
        experimentService.saveExperiment(exp);
        // END - Experiment section

        // remove old run

        // Grammar File SECTION
        String grammarFilePath = grammarFileSection(user, configExpDto, grammar);
        // END - Grammar File SECTION

        // Create ExpPropertiesDto file
        expPropertiesSet(radioDataTypeHidden, fileModelDto, configExpDto, user, currentTimestamp, expDataType, exp, grammarFilePath, run);

        List<Thread> threads = new ArrayList<>();
        // Run experiment in new thread
        threads.add(runExperimentDetails(user, run, run.getDiagramData()));
        //check if need to run more runs
        for (int i = 1; i < configExpDto.getNumberRuns(); i++) {
            // RUN SECTION
            Run newRun = runService.saveRun(new Run());
            runSection(newRun, grammar, configExpDto, currentTimestamp);
            exp.getIdRunList().add(newRun);
            newRun.setExperimentId(exp);

            expDataType.setRunId(newRun.getId());
            newRun.setDefaultExpDataTypeId(expDataType.getId());
            // Create ExpPropertiesDto file
            expPropertiesSet(radioDataTypeHidden, fileModelDto, configExpDto, user, currentTimestamp, expDataType, exp, grammarFilePath, newRun);

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

            }
        });
        thread.start();

        redirectAttrs.addAttribute("idRun", run.getId()).addFlashAttribute("configuration",
                "Experiment is being created");
        return "redirect:/experiment/redirectConfigExperiment";
    }

    protected void expPropertiesSet(@RequestParam("radioDataType") String radioDataTypeHidden, @ModelAttribute("typeFile") FileModelDto fileModelDto, @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto, User user, Timestamp currentTimestamp, ExperimentDataType expDataType, Experiment exp, String grammarFilePath, Run newRun) throws IOException {
        ExpPropertiesDto newPropertiesDto = new ExpPropertiesDto(user,
                0.0, configExpDto.getTournament(), 0,
                configExpDto.getCrossoverProb(), grammarFilePath, 0,
                1, configExpDto.getMutationProb(), false, 1,
                configExpDto.getNumCodons(), configExpDto.getPopulationSize(),
                configExpDto.getGenerations(), false, configExpDto.getMaxWraps(),
                500, configExpDto.getExperimentName(),
                configExpDto.getExperimentDescription());
        fileConfig(expDataType, user, newPropertiesDto, configExpDto, currentTimestamp, fileModelDto, radioDataTypeHidden,
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
            model.addAttribute("type", new ExperimentDataType());
            model.addAttribute("configuration", new ConfigExperimentDto());
            model.addAttribute("user", user);
            return "experiment/configExperiment";
        }
        Grammar grammar = experimentService.findGrammarById(run.getExperimentId().getDefaultGrammar());
        ExperimentDataType expDataType = experimentService.findExperimentDataTypeById(run.getExperimentId().getDefaultExpDataType());

        List<Run> runList = run.getExperimentId().getIdRunList();
        List<ExperimentDataType> expDataTypeList = run.getExperimentId().getIdExpDataTypeList();

        ConfigExperimentDto configExpDto = fillConfigExpDto(new ConfigExperimentDto(),
                run.getExperimentId(), run, grammar, expDataType);

        model.addAttribute("configuration", configExpDto);
        model.addAttribute("runList", runList);
        model.addAttribute("dataTypeList", expDataTypeList);
        model.addAttribute("configExp", configExpDto);
        modelAddData(model, user, grammar, expDataType, expDataTypeList);

        return "experiment/configExperiment";
    }

    @RequestMapping(value = "/experiment/start", method = RequestMethod.POST, params = "saveExperimentButton")
    public String saveExperiment(Model model,
                                 @RequestParam("grammarId") String grammarId,
                                 @RequestParam("experimentDataTypeId") String experimentDataTypeId,
                                 @ModelAttribute("type") ExperimentDataTypeDto expDataTypeDto,
                                 @RequestParam("radioDataType") String radioDataTypeHidden,
                                 @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                 @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto,
                                 BindingResult result) throws IllegalStateException, IOException {

        User user = userService.getLoggedInUser();
        modelAddData(model, user, null, null, null);

        if (result.hasErrors()) {
            model.addAttribute("configuration", configExpDto);
            return "experiment/configExperiment";
        }

        Experiment exp = null;
        model.addAttribute("configuration", configExpDto);
        model.addAttribute("expConfig", configExpDto);

        // Grammar section
        Grammar updGrammar = experimentService.findGrammarById(Long.valueOf(grammarId));

        // DATE TIMESTAMP
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());

        // Experiment Data Type SECTION
        ExperimentDataType expDataType;
        if (experimentDataTypeId == "-1") {
            result.rejectValue("typeFile", "error.typeFile", "Choose one file");
            return "experiment/configExperiment";
        } else {
            expDataType = experimentService.findDataTypeById(Long.parseLong(experimentDataTypeId));
        }

        expDataType = experimentDataTypeSection(fileModelDto, expDataType, expDataTypeDto, currentTimestamp);
        // END - Experiment Data Type SECTION

        // Experiment section:
        if (configExpDto.getId() != null) {
            exp = experimentService.findExperimentById(configExpDto.getId());
        }
        exp = experimentSection(exp, user, expDataType, configExpDto, updGrammar,
                null, currentTimestamp, null);
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

        fileConfig(expDataType, user, propertiesDto, configExpDto, currentTimestamp, fileModelDto, radioDataTypeHidden,
                null, exp);


        modelAddData(model, user, grammarRepository.findGrammarById(Long.valueOf(grammarId)),
                experimentService.findExperimentDataTypeById(Long.valueOf(experimentDataTypeId)),
                exp.getIdExpDataTypeList());
        return "experiment/configExperiment";

    }

    @RequestMapping(value = "/experiment/start", method = RequestMethod.POST, params = "cloneExperimentButton")
    public String cloneExperiment(Model model,
                                  @RequestParam("grammarId") String grammarId,
                                  @RequestParam("experimentDataTypeId") String experimentDataTypeId,
                                  @ModelAttribute("type") ExperimentDataTypeDto expDataTypeDto,
                                  @RequestParam("radioDataType") String radioDataTypeHidden,
                                  @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                  @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto,
                                  BindingResult result) throws IllegalStateException, IOException {
        User user=userService.getLoggedInUser();
        configExpDto.setId(null);
        configExpDto.setDefaultRunId(null);

        model.addAttribute("configuration", configExpDto);
        model.addAttribute("expConfig", configExpDto);
        modelAddData(model, user, grammarRepository.findGrammarById(Long.valueOf(grammarId)),
                experimentService.findExperimentDataTypeById(Long.valueOf(experimentDataTypeId)),
                null);
        model.addAttribute("disabledClone", true);
        model.addAttribute("messageClone", "This experiment is cloned and not saved yet.");
        return "experiment/configExperiment";
    }


    private void fileConfig(ExperimentDataType expDataType, User user, ExpPropertiesDto propertiesDto,
                            ConfigExperimentDto configExpDto, java.sql.Timestamp currentTimestamp,
                            FileModelDto fileModelDto, String radioDataTypeHidden, Run run, Experiment exp) throws IOException {
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
        createPropertiesFile(propertiesFilePath, propertiesDto, configExpDto.getExperimentName(), currentTimestamp);  // Write in property file
        // END - Create ExpPropertiesDto file

        // MultipartFile section
        MultipartFile multipartFile = fileModelDto.getTypeFile();
        String dataFilePath;

        // If Radio button and file path selected -> File path is selected
        // NULL -> didn't select the dataType file from the list - ON if th:value in input is empty
        if ((multipartFile.getOriginalFilename() == "null") && (radioDataTypeHidden.equals("on") && !multipartFile.isEmpty()) || (!radioDataTypeHidden.equals("on") && !multipartFile.isEmpty())) {
            File tmpFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") +
                    multipartFile.getOriginalFilename());
            multipartFile.transferTo(tmpFile);

            dataFilePath = tmpFile.getAbsolutePath();
            Reader reader = new FileReader(tmpFile);
            experimentService.loadExperimentRowTypeFile(reader, expDataType);   // Save row here
            reader.close();
        } else {   // DataType selected from list
            experimentService.loadExperimentRowType(expDataType);
            List<ExperimentRowType> lExpRowType = expDataType.getListRowsFile();
            // Create temporal training path file
            File tmpFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") +
                    "trainingPathFile.csv");
            tmpFile.createNewFile();

            FileWriter fWriter = new FileWriter(tmpFile, false);    // true = append; false = overwrite
            BufferedWriter writer = new BufferedWriter(fWriter);

            writer.append(expDataType.headerToString());

            for (ExperimentRowType e : lExpRowType)
                writer.append(e.toString());
            writer.close();
            dataFilePath = tmpFile.getAbsolutePath();
        }
        // END Reader - FILE DATA TYPE
        // END - Multipart File Section
        // END CONFIGURATION SECTION

        // Execute program with experiment info
        File propertiesFile = new File(propertiesFilePath);
        Reader propertiesReader = new FileReader(propertiesFile);

        Properties properties = new Properties();
        properties.load(propertiesReader);

        properties.setProperty(TRAINING_PATH_PROP, dataFilePath);

        ExpProperties expPropertiesEntity = experimentService.saveExpProperties(new ExpProperties());
        createExpPropertiesEntity(expPropertiesEntity, properties, exp, run, propertiesDto, dataFilePath);

        propertiesReader.close();
    }


    @RequestMapping(value = "/experiment/experimentRepository", method = RequestMethod.GET)
    public String experimentRepository(Model model) {

        User user = userService.getLoggedInUser();
        List<Experiment> lExperiment = experimentService.findByUserId(user);
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
        ExperimentDataType expDataType = experimentService.findExperimentDataTypeById(exp.getDefaultExpDataType());
        List<Run> runList = exp.getIdRunList();

        ConfigExperimentDto configExpDto = fillConfigExpDto(new ConfigExperimentDto(), exp,
                exp.getIdRunList().isEmpty() ? null : runService.findByRunId(exp.getDefaultRunId()),
                grammar, expDataType);

        modelAddData(model, user, grammar, experimentService.findExperimentDataTypeById(exp.getDefaultExpDataType()),
                exp.getIdExpDataTypeList());
        model.addAttribute("runList", runList);
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
            experimentService.deleteExpProperties(experimentService.findPropertiesById(runIt.getIdProperties()));
            diagramDataService.deleteDiagram(runIt.getDiagramData());
        }

        Iterator<Grammar> listGrammarIt = expConfig.getIdGrammarList().iterator();
        while (listGrammarIt.hasNext()) {
            Grammar grammarIt = listGrammarIt.next();
            listGrammarIt.remove();
            grammarIt.deleteExperimentId(expConfig);
        }

        Iterator<ExperimentDataType> listDataTypeIt = expConfig.getIdExpDataTypeList().iterator();
        while (listDataTypeIt.hasNext()) {
            ExperimentDataType expData = listDataTypeIt.next();
            listDataTypeIt.remove();
            expData.deleteExperimentInList(expConfig);
        }

        expConfig.setDefaultGrammar(null);
        experimentService.saveExperiment(expConfig);
        experimentService.deleteExperiment(expConfig);
        return idExp;
    }

    @RequestMapping(value = "/experiment/runList", method = RequestMethod.GET, params = "loadExperimentButton")
    public String loadExperiment(Model model,
                                 @RequestParam("runId") String runId) {
        User user = userService.getLoggedInUser();
        Long longRunId = Long.parseLong(runId);
        Run run = runService.findByRunId(longRunId);

        Grammar grammar = experimentService.findGrammarById(run.getDefaultGrammarId());
        ExperimentDataType expDataType = experimentService.findExperimentDataTypeById(run.getDefaultExpDataTypeId());
        List<Run> runList = run.getExperimentId().getIdRunList();

        run.setDefaultRunId(longRunId);
        run.getExperimentId().setDefaultGrammar(grammar.getId());
        run.getExperimentId().setDefaultExpDataType(expDataType.getId());
        run.getExperimentId().setDefaultRunId(longRunId);   // To know what run to load

        ConfigExperimentDto configExpDto = new ConfigExperimentDto();
        configExpDto = fillConfigExpDto(configExpDto, run.getExperimentId(), run, grammar, expDataType);

        experimentService.findExperimentDataTypeById(run.getDefaultExpDataTypeId());

        modelAddData(model,
                user,
                grammar,
                experimentService.findExperimentDataTypeById(run.getDefaultExpDataTypeId()),
                null);
        model.addAttribute("configuration", configExpDto);
        model.addAttribute("configExp", configExpDto);
        model.addAttribute("runList", runList);
        model.addAttribute("dataTypeList", run.getExperimentId().getIdExpDataTypeList());

        return "experiment/configExperiment";
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
        experimentDetailsDto.setBestIndividual(diagramData.getBestIndividual());
        experimentDetailsDto.setCurrentGeneration(diagramData.getCurrentGeneration());
        return experimentDetailsDto;
    }

    @GetMapping(value = "/experiment/runList", params = "showPlotExecutionButton")
    public String showPlotExecutionExperiment(Model model,
                                              @RequestParam(value = "runId") String runId) {
        Long longRunId = Long.parseLong(runId);
        Run run = runService.findByRunId(longRunId);
        DiagramData diagramData = diagramDataService.findByRunId(run);

        if (run.getStatus().equals(Run.Status.RUNNING)) {
            ExperimentDetailsDto experimentDetailsDto = setExperimentDetailDto(run, diagramData);
            model.addAttribute("expDetails", experimentDetailsDto);
            return "experiment/experimentDetails";
        }

        ExperimentDetailsDto experimentDetailsDto = setExperimentDetailDto(run, diagramData);
        model.addAttribute("expDetails", experimentDetailsDto);

        return "experiment/showDiagramPlot";
    }

    public Thread runExperimentDetails(User user, Run run, DiagramData diagramData) throws IOException {
        ExpProperties prop = experimentService.findPropertiesById(run.getIdProperties());
        String propertiesFilePath = PROPERTIES_DIR_PATH + user.getId() + File.separator + run.getExperimentName().replaceAll("\\s+", "") + "_" + prop.getUuidPropDto() + ".properties";

        File propertiesFile = new File(propertiesFilePath);
        Reader propertiesReader = new FileReader(propertiesFile);

        Properties properties = new Properties();
        properties.load(propertiesReader);
        properties.setProperty(TRAINING_PATH_PROP, prop.getTrainingPath());

        RunnableExpGramEv obj = new RunnableExpGramEv(properties, diagramData, run);
        Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread th, Throwable ex) {
                run.setStatus(Run.Status.FAILED);
                run.getDiagramData().setFailed(true);
                System.out.println("Uncaught exception: " + ex);
            }
        };
        Thread th = new Thread(obj);
        th.setUncaughtExceptionHandler(h);
        threadMap.put(th.getId(), th);
        run.setThreaId(th.getId());
        runnables.put(th.getId(), obj);
        // https://stackoverflow.com/questions/26213615/terminating-thread-using-thread-id-in-java

        propertiesReader.close();
        return th;
    }

    public void createPropertiesFile(String propertiesFilePath, ExpPropertiesDto propertiesDto, String expName, java.sql.Timestamp currentTimeStamp) throws IOException {
        File propertiesNewFile = new File(propertiesFilePath);
        if (!propertiesNewFile.exists()) {
            propertiesNewFile.createNewFile();
        }

        PrintWriter propertiesWriter = new PrintWriter(propertiesNewFile);

        propertiesWriter.println("# ExpPropertiesDto for " + expName);
        propertiesWriter.println("# " + currentTimeStamp.toString());
        propertiesWriter.println("LoggerBasePath=" + propertiesDto.getLoggerBasePath().replace("\\", "/"));
        propertiesWriter.println("ErrorThreshold=" + propertiesDto.getErrorThreshold());
        propertiesWriter.println("TournamentSize=" + propertiesDto.getTournamentSize());
        propertiesWriter.println("WorkDir=" + propertiesDto.getWorkDir().replace("\\", "/"));
        propertiesWriter.println("RealDataCopied=" + propertiesDto.getRealDataCopied());
        propertiesWriter.println("CrossoverProb=" + propertiesDto.getCrossoverProb());
        propertiesWriter.println("BnfPathFile=" + propertiesDto.getBnfPathFile().substring(2, propertiesDto.getBnfPathFile().length()).replace("\\", "/"));
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
            propertiesWriter.println("TrainingPath=" + propertiesDto.getTrainingPath().substring(2, propertiesDto.getTrainingPath().length()).replace("\\", "/"));
        else if (propertiesDto.getValidation()) {
            propertiesWriter.println("TrainingPath=" + propertiesDto.getTrainingPath().substring(2, propertiesDto.getTrainingPath().length()).replace("\\", "/")); // TEMPORAL UNTIL KNOW IF WE NEED THIS OR NOT
            propertiesWriter.println("ValidationPath=" + propertiesDto.getValidationPath().substring(2, propertiesDto.getValidationPath().length()).replace("\\", "/"));
        } else {
            propertiesWriter.println("TrainingPath=" + propertiesDto.getTrainingPath().substring(2, propertiesDto.getTrainingPath().length()).replace("\\", "/"));
            propertiesWriter.println("TestPath=" + propertiesDto.getTestPath().substring(2, propertiesDto.getTestPath().length()).replace("\\", "/"));
            // ("\\", "/")
        }
        propertiesWriter.close();
    }

    public ExpProperties createExpPropertiesEntity(ExpProperties expProp, Properties properties,
                                                   Experiment experiment,
                                                   Run run, ExpPropertiesDto propDto, String dataFilePath) {
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

        expProp.setTrainingPath(dataFilePath);

        expProp.setExperimentName(experiment.getExperimentName());
        expProp.setExperimentDescription(experiment.getExperimentDescription());
        expProp.setInitialization(experiment.getInitialization());
        expProp.setResults(experiment.getResults());
        expProp.setNumberRuns(experiment.getNumberRuns());

        return expProp;
    }

    public Run runSection(Run run, Grammar grammar, ConfigExperimentDto configExpDto, java.sql.Timestamp currentTimestamp) {
        run.setDefaultRunId(run.getId());
        run.setStatus(Run.Status.INITIALIZING);

        run.setIniDate(currentTimestamp);
        run.setModificationDate(currentTimestamp);

        run.setExperimentName(configExpDto.getExperimentName());
        run.setExperimentDescription(configExpDto.getExperimentDescription());

        run.setDefaultGrammarId(grammar.getId());
        grammar.setRunId(run.getId());

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

        return run;
    }

    public String grammarFileSection(User user, ConfigExperimentDto configExpDto, Grammar grammar) throws IllegalStateException, IOException {

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

    public ExperimentDataType experimentDataTypeSection(FileModelDto fileModelDto, ExperimentDataType expDataType, ExperimentDataTypeDto expDataTypeDto, java.sql.Timestamp currentTimestamp) throws IOException {
        if (!fileModelDto.getTypeFile().isEmpty()) {
            expDataType.setDataTypeName(expDataTypeDto.getDataTypeName());
            expDataType.setinfo(expDataTypeDto.getinfo());
            expDataType.setDataTypeDescription(expDataTypeDto.getDataTypeDescription());
            expDataType.setCreationDate(currentTimestamp);
            expDataType.setDataTypeType("training");
        } else if (expDataType.getRunId() != null) {
            expDataType.setModificationDate(currentTimestamp);
            expDataType.setDataTypeType(expDataTypeDto.getDataTypeType().toString());
        }

        return expDataType;
    }

    public Experiment experimentSection(Experiment exp, User user, ExperimentDataType expDataType,
                                        ConfigExperimentDto configExpDto, Grammar grammar, Run run,
                                        java.sql.Timestamp currentTimestamp, Long longDefaultRunId) {
        if (exp == null) {   // We create it
            exp = new Experiment(user, configExpDto.getExperimentName(), configExpDto.getExperimentDescription(), configExpDto.getGenerations(),
                    configExpDto.getPopulationSize(), configExpDto.getMaxWraps(), configExpDto.getTournament(), configExpDto.getCrossoverProb(), configExpDto.getMutationProb(),
                    configExpDto.getInitialization(), configExpDto.getResults(), configExpDto.getNumCodons(), configExpDto.getNumberRuns(), configExpDto.getObjective(), currentTimestamp, currentTimestamp);
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

            exp.setCreationDate(currentTimestamp);
            exp.setModificationDate(currentTimestamp);

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

        return exp;
    }

    private void removeRuns(Experiment exp) {
        List<Run> oldRunList = new ArrayList<>();
        oldRunList.addAll(exp.getIdRunList());
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

    @GetMapping("/experiment/experimentDetails")
    public String experimentDetails(@ModelAttribute("expDetails") ExperimentDetailsDto expDetailsDto) {
        return "experiment/experimentDetails";
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
        run.setStatus(Run.Status.STOPPED);
        run.getDiagramData().setStopped(true);
        runService.saveRun(run);

        th.interrupt();
        runnables.get(threadId).stopExecution();

        ExperimentDetailsDto experimentDetailsDto = setExperimentDetailDto(run, run.getDiagramData());

        model.addAttribute("expDetails", experimentDetailsDto);
        return "experiment/showDiagramPlot";
    }

    @RequestMapping(value = "/experiment/expRepoSelected", method = RequestMethod.POST, params = "deleteRun")
    public
    @ResponseBody
    Long deleteRun(@RequestParam("runId") String runId) {
        Boolean found = false;

        Long longRunId = Long.parseLong(runId);
        Run run = runService.findByRunId(longRunId);
        Experiment experiment = run.getExperimentId();

        List<Grammar> lGrammar = experiment.getIdGrammarList();
        Iterator<Grammar> grammarIt = lGrammar.iterator();
        while (grammarIt.hasNext() && !found) {
            Grammar grammarAux = grammarIt.next();
            if (grammarAux.getId().longValue() == run.getDefaultGrammarId().longValue()) {
                grammarAux.deleteExperimentId(experiment);

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
        if (experiment.getDefaultRunId() == longRunId) {
            experiment.setDefaultRunId(null);
            experimentService.saveExperiment(experiment);
        }
        return longRunId;
    }

    public ConfigExperimentDto fillConfigExpDto(ConfigExperimentDto configExpDto, Experiment exp, Run run, Grammar grammar, ExperimentDataType expDataType) {
        if (run == null) {
            configExpDto.setDefaultRunId(exp.getDefaultRunId());
            configExpDto = setConfigExpDtoWIthExperiment(configExpDto, exp.getExperimentName(),
                    exp.getExperimentDescription(), exp.getCrossoverProb(), exp.getGenerations(),
                    exp.getPopulationSize(), exp.getMaxWraps(), exp.getTournament(), exp.getMutationProb(),
                    exp.getInitialization(), exp.getResults(), exp.getNumCodons(), exp.getNumberRuns(), exp.getObjective(), exp);
        } else {
            configExpDto.setDefaultRunId(run.getId());
            configExpDto = setConfigExpDtoWIthExperiment(configExpDto, run.getExperimentName(),
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

        configExpDto.setDataTypeName(expDataType.getDataTypeName());
        configExpDto.setinfo(expDataType.getinfo());
        configExpDto.setDataTypeDescription(expDataType.getDataTypeDescription());

        return configExpDto;
    }

    private ConfigExperimentDto setConfigExpDtoWIthExperiment(ConfigExperimentDto configExpDto, String experimentName, String experimentDescription, Double crossoverProb, Integer generations, Integer populationSize, Integer maxWraps, Integer tournament, Double mutationProb, String initialization, String results, Integer numCodons, Integer numberRuns, String objective, Experiment exp) {
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
        return configExpDto;
    }

    public static HashMap<Long, RunnableExpGramEv> getRunnables() {
        return runnables;
    }
}