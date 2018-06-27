package com.gramevapp.web.controller;

import com.engine.algorithm.RunnableExpGramEv;
import com.gramevapp.web.model.*;
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

import javax.validation.Valid;
import java.io.*;
import java.util.*;

import static com.engine.util.Common.TRAINING_PATH_PROP;

@Controller
public class ExperimentController {

    private HashMap<Long, Thread> threadMap = new HashMap();
    private HashMap<Long, RunnableExpGramEv> runnables = new HashMap();

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    @Autowired
    private RunService runService;

    @Autowired
    private DiagramDataService diagramDataService;

    @ModelAttribute
    public FileModelDto fileModel(){
        return new FileModelDto();
    }

    private final String GRAMMAR_DIR_PATH = "." + File.separator + "resources" + File.separator + "files" + File.separator + "grammar" + File.separator + "";
    private final String DATATYPE_DIR_PATH = "." + File.separator + "resources" + File.separator + "files" + File.separator + "dataType" + File.separator + "";
    private final String PROPERTIES_DIR_PATH = "." + File.separator + "resources" + File.separator + "files" + File.separator + "properties" + File.separator + "";

    @GetMapping("/user/experiment/configExperiment")
    public String configExperiment(Model model,
                                   @ModelAttribute("configuration") ConfigExperimentDto configExpDto){

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        // WE NEED TO ADD HERE THE EXPERIMENT INFO TO SEND IT TO configExperiment
        Experiment expConfig = experimentService.findExperimentById(configExpDto.getId());
        Grammar grammarDto;
        ExperimentDataType expDataTypeDto;
        List<Run> runList;
        List<ExperimentDataType> expDataTypeList;

        if(expConfig != null) {
            grammarDto = experimentService.findGrammarById(expConfig.getDefaultGrammar());
            expDataTypeDto = experimentService.findExperimentDataTypeById(expConfig.getDefaultExpDataType());

            runList = expConfig.getIdRunList();
            expDataTypeList = expConfig.getIdExpDataTypeList();
        }
        else {
            grammarDto = new Grammar();
            expDataTypeDto = new ExperimentDataType();

            runList = new ArrayList();
            expDataTypeList = new ArrayList();
        }

        model.addAttribute("grammar", grammarDto);
        model.addAttribute("type", expDataTypeDto);
        model.addAttribute("configuration", configExpDto);
        model.addAttribute("runList", runList);
        model.addAttribute("dataTypeList", expDataTypeList);
        model.addAttribute("user", user);
        model.addAttribute("configExp", new ConfigExperimentDto());

        return "/user/experiment/configExperiment";
    }

    @GetMapping("/user/experiment/redirectConfigExperiment")
    public String redirectViewConfigExperiment(Model model,
                                   @ModelAttribute("idRun") String idRun){

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        Run run = runService.findByRunId(Long.parseLong(idRun));
        if(run == null) { // Run eliminated
            model.addAttribute("grammar",  new Grammar());
            model.addAttribute("type",  new ExperimentDataType());
            model.addAttribute("configuration", new ConfigExperimentDto());
            model.addAttribute("user", user);
            return "/user/experiment/configExperiment";
        }
        Grammar grammar = experimentService.findGrammarById(run.getExperimentId().getDefaultGrammar());
        ExperimentDataType expDataType = experimentService.findExperimentDataTypeById(run.getExperimentId().getDefaultExpDataType());

        List<Run> runList = run.getExperimentId().getIdRunList();
        List<ExperimentDataType> expDataTypeList = run.getExperimentId().getIdExpDataTypeList();

        ConfigExperimentDto configExpDto = new ConfigExperimentDto();
        configExpDto = fillConfigExpDto(configExpDto, run.getExperimentId(), run, grammar, expDataType);

        model.addAttribute("configuration", configExpDto);
        model.addAttribute("runList", runList);
        model.addAttribute("dataTypeList", expDataTypeList);
        model.addAttribute("configExp", configExpDto);

        return "/user/experiment/configExperiment";
    }

    @RequestMapping(value="/user/experiment/start")
    public String saveExperiment(Model model){
        model.addAttribute("configuration", new ConfigExperimentDto());
        model.addAttribute("configExp", new ConfigExperimentDto());
        return "/user/experiment/configExperiment";
    }

    @RequestMapping(value="/user/experiment/start", method=RequestMethod.POST, params="saveExperimentButton")
    public String saveExperiment(Model model,
                                @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto,
                                BindingResult result) throws IllegalStateException {

        if (result.hasErrors()) {
            model.addAttribute("configuration", configExpDto);
            return "/user/experiment/configExperiment";
        }

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        Run updRun = runService.findByRunId(configExpDto.getDefaultRunId());
        if(updRun == null) { // Do not update nothing
            model.addAttribute("messageSave", "To save the experiment, first you need to create one");
            model.addAttribute("configuration", configExpDto);
            model.addAttribute("expConfig", configExpDto);
            return "/user/experiment/configExperiment";
        }

        // DATE TIMESTAMP
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp modificationDate = new java.sql.Timestamp(calendar.getTime().getTime());

        // Properties section
        updRun.setExperimentName(configExpDto.getExperimentName());
        updRun.setExperimentDescription(configExpDto.getExperimentDescription());
        updRun.setGenerations(configExpDto.getGenerations());
        updRun.setPopulationSize(configExpDto.getPopulationSize());
        updRun.setMaxWraps(configExpDto.getMaxWraps());
        updRun.setTournament(configExpDto.getTournament());
        updRun.setCrossoverProb(configExpDto.getCrossoverProb());
        updRun.setMutationProb(configExpDto.getMutationProb());
        updRun.setInitialization(configExpDto.getInitialization());
        updRun.setResults(configExpDto.getResults());
        updRun.setNumCodons(configExpDto.getNumCodons());
        updRun.setNumberRuns(configExpDto.getNumberRuns());
        updRun.setObjective(configExpDto.getObjective());
        updRun.setModificationDate(modificationDate);

        updRun.setDefaultGrammarId(configExpDto.getDefaultGrammarId());
        updRun.setDefaultExpDataTypeId(configExpDto.getDefaultExpDataTypeId());
        updRun.setDefaultRunId(updRun.getId());

        // Grammar section
        Grammar updGrammar = experimentService.findGrammarById(updRun.getDefaultGrammarId());
        updGrammar.setGrammarName(configExpDto.getGrammarName());
        updGrammar.setGrammarDescription(configExpDto.getGrammarDescription());
        updGrammar.setFileText(configExpDto.getFileText());

        // DataType section
        ExperimentDataType updExpDataType = experimentService.findExperimentDataTypeById(updRun.getDefaultExpDataTypeId());
        updExpDataType.setDataTypeName(configExpDto.getDataTypeName());
        updExpDataType.setDataTypeDescription(configExpDto.getDataTypeDescription());

        experimentService.saveGrammar(updGrammar);
        experimentService.updateGrammar();
        experimentService.saveDataType(updExpDataType);
        experimentService.updataExpDataType();
        runService.saveRun(updRun);
        runService.updateRun();

        configExpDto = fillConfigExpDto(configExpDto, updRun.getExperimentId(), updRun, updGrammar, updExpDataType);

        model.addAttribute("expConfig", configExpDto);
        model.addAttribute("configuration", configExpDto);
        model.addAttribute("dataTypeList", updRun.getExperimentId().getIdExpDataTypeList());
        model.addAttribute("runList", updRun.getExperimentId().getIdRunList());

        return "/user/experiment/configExperiment";
    }

    /**
     * Load al the data from the view, save it and run the application.
     *  "configExpDto" for validation -> configExp
     *  "configuration" is for send data from Controller to View and
     *  "configExp" is the object from the form View
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
    @RequestMapping(value="/user/experiment/start", method=RequestMethod.POST, params="runExperimentButton")
    public String runExperiment(Model model,
                                @ModelAttribute("grammar") GrammarDto grammarDto,
                                @ModelAttribute("type") ExperimentDataTypeDto expDataTypeDto,
                                @RequestParam("radioDataType") String radioDataTypeHidden,
                                @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                @ModelAttribute("configExp") @Valid ConfigExperimentDto configExpDto,
                                BindingResult result,
                                RedirectAttributes redirectAttrs) throws IllegalStateException, IOException {
        if (result.hasErrors()) {
            model.addAttribute("configuration", configExpDto);
            return "/user/experiment/configExperiment";
        }

        if(radioDataTypeHidden.equals("on") && fileModelDto.getTypeFile().isEmpty()) {        // Radio button neither file path selected
            result.rejectValue("typeFile", "error.typeFile", "Choose one file");
            return "/user/experiment/configExperiment";
        }

        User user = userService.getLoggedInUser();
        if (user == null) {
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        // CONFIGURATION SECTION
        // GRAMMAR SECTION
        Grammar grammar = experimentService.saveGrammar(new Grammar());
        grammar = grammarSection(grammar, grammarDto);
        experimentService.updateGrammar();

        // DATE TIMESTAMP
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());

        // RUN SECTION
        Run run = runService.saveRun(new Run());
        runSection(run, grammar, configExpDto, currentTimestamp);
        runService.updateRun();

        // Experiment Data Type SECTION
        ExperimentDataType expDataType = experimentService.saveDataType(new ExperimentDataType());
        experimentService.updataExpDataType();
        if(!radioDataTypeHidden.equals("on"))
            expDataType = experimentService.findDataTypeById(Long.parseLong(radioDataTypeHidden));
        else if(radioDataTypeHidden.equals("on") && fileModelDto.getTypeFile().isEmpty()) {        // Radio button neither file path selected
            result.rejectValue("typeFile", "error.typeFile", "Choose one file");
            return "/user/experiment/configExperiment";
        }
        expDataType = experimentDataTypeSection(fileModelDto, expDataType, expDataTypeDto, currentTimestamp);
        expDataType.setRunId(run.getId());
        run.setDefaultExpDataTypeId(expDataType.getId());
        // END - Experiment Data Type SECTION

        // Experiment section
        Experiment exp = experimentService.findExperimentById(configExpDto.getId());
        exp = experimentSection(exp, user, expDataType, configExpDto, grammar, run, currentTimestamp, run.getId());
        // END - Experiment section

        // Grammar File SECTION
        String grammarFilePath = grammarFileSection(user, configExpDto, grammar);
        // END - Grammar File SECTION

        // Create ExpPropertiesDto file
        ExpPropertiesDto propertiesDto = new ExpPropertiesDto(user, 0.0, configExpDto.getTournament(), 0, configExpDto.getCrossoverProb(), grammarFilePath, 0, 1, configExpDto.getMutationProb(), false, 1, configExpDto.getNumCodons(), configExpDto.getPopulationSize(), configExpDto.getGenerations(), false, configExpDto.getMaxWraps(), 500, configExpDto.getExperimentName(), configExpDto.getExperimentDescription());

        experimentService.saveExperiment(exp);
        experimentService.updateExperiment();  // Update exp

        // Reader - FILE DATA TYPE - Convert MultipartFile into Generic Java File - Then convert it to Reader
        String dataTypeDirectoryPath = DATATYPE_DIR_PATH;
        if(expDataType.getDataTypeType().equals("validation")) {
            dataTypeDirectoryPath += "validation\\" + user.getId();
            propertiesDto.setValidationPath(dataTypeDirectoryPath);
            propertiesDto.setValidation(true);
        }
        else if(expDataType.getDataTypeType().equals("test")){
            dataTypeDirectoryPath += "test\\" + user.getId();
            propertiesDto.setTestPath(dataTypeDirectoryPath);
            propertiesDto.setTest(true);
        }
        else {
            dataTypeDirectoryPath += "training\\" + user.getId();
            propertiesDto.setTrainingPath(dataTypeDirectoryPath + File.separator + configExpDto.getExperimentName() + "_" + expDataType.getId() + ".csv");
            propertiesDto.setTraining(true);
        }

        File dir = new File(PROPERTIES_DIR_PATH + user.getId());
        if (!dir.exists())
            dir.mkdirs();

        String propertiesFilePath = PROPERTIES_DIR_PATH + user.getId() + File.separator + configExpDto.getExperimentName().replaceAll("\\s+","") + "_" + propertiesDto.getId() + ".properties";
        createPropertiesFile(propertiesFilePath, propertiesDto, configExpDto.getExperimentName(), currentTimestamp);  // Write in property file
        // END - Create ExpPropertiesDto file

        // MultipartFile section
        MultipartFile multipartFile = fileModelDto.getTypeFile();
        String dataFilePath;

        // If Radio button and file path selected -> File path is selected
        // NULL -> didn't select the dataType file from the list - ON if th:value in input is empty
        if( (radioDataTypeHidden.equals("on") && !multipartFile.isEmpty()) || (!radioDataTypeHidden.equals("on") && !multipartFile.isEmpty()) ) {
            File tmpFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") +
                    multipartFile.getOriginalFilename());
            multipartFile.transferTo(tmpFile);

            dataFilePath = tmpFile.getAbsolutePath();
            Reader reader = new FileReader(tmpFile);
            experimentService.loadExperimentRowTypeFile(reader, expDataType);   // Save row here
            reader.close();
        }
        else{   // DataType selected from list
            List<ExperimentRowType> lExpRowType = expDataType.getListRowsFile();

            // Create temporal training path file
            File tmpFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") +
                    "trainingPathFile.csv");
            tmpFile.createNewFile();

            FileWriter fWriter = new FileWriter(tmpFile, false);    // true = append; false = overwrite
            BufferedWriter writer = new BufferedWriter(fWriter);

            writer.append(expDataType.headerToString());

            for(ExperimentRowType e : lExpRowType)
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

        DiagramData diagramData = diagramDataService.saveDiagram(new DiagramData());
        diagramDataService.flushDiagramData();
        diagramData.setRunId(run);

        // Run experiment in new thread
        runExperimentDetails(user, run, run.getDiagramData());

        redirectAttrs.addAttribute("idRun", run.getId()).addFlashAttribute("configuration", "Experiment is being created");
        return "redirect:/user/experiment/redirectConfigExperiment";
    }

    @RequestMapping(value="/user/experiment/experimentRepository", method=RequestMethod.GET)
    public String experimentRepository(Model model){

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        List<Experiment> lExperiment = experimentService.findByUserId(user);

        model.addAttribute("experimentList", lExperiment);
        model.addAttribute("user", user);

        return "/user/experiment/experimentRepository";
    }

    @RequestMapping(value="/user/experiment/expRepoSelected", method=RequestMethod.GET, params="loadExperimentButton")
    public String expRepoSelected(Model model,
                                  @RequestParam(required=false) String id){ // Exp ID

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        if(id == null)
            return "redirect:/user/experiment/experimentRepository";

        Experiment exp = experimentService.findExperimentByUserIdAndExpId(user, Long.parseLong(id));

        Grammar grammar = experimentService.findGrammarById(exp.getDefaultGrammar());
        ExperimentDataType expDataType = experimentService.findExperimentDataTypeById(exp.getDefaultExpDataType());
        List<Run> runList = exp.getIdRunList();

        ConfigExperimentDto configExpDto = new ConfigExperimentDto();
        configExpDto = fillConfigExpDto(configExpDto, exp, runService.findByRunId(exp.getDefaultRunId()), grammar, expDataType);

        model.addAttribute("runList", runList);
        model.addAttribute("configuration", configExpDto);
        model.addAttribute("configExp", configExpDto);
        model.addAttribute("runList", runList);
        model.addAttribute("dataTypeList", exp.getIdExpDataTypeList());

        return "/user/experiment/configExperiment";
    }

    @RequestMapping(value="/user/experiment/expRepoSelected", method=RequestMethod.POST, params="deleteExperiment")
    public
    @ResponseBody Long expRepoSelectedDelete(@RequestParam("experimentId") String experimentId){
        Long idExp = Long.parseLong(experimentId);

        Experiment expConfig = experimentService.findExperimentById(idExp);

        Iterator<Run> listRunIt = expConfig.getIdRunList().iterator();
        while(listRunIt.hasNext()) {
            Run runIt = listRunIt.next();
            Run run = runService.findByRunId(runIt.getId());
            Long threadId = run.getThreaId();
            // https://stackoverflow.com/questions/26213615/terminating-thread-using-thread-id-in-java
            Thread th = threadMap.get(threadId);
            th.interrupt();
            runnables.get(threadId).stopExecution();

            run.setStatus(Run.Status.STOPPED);
            experimentService.deleteExpProperties(experimentService.findPropertiesById(runIt.getIdProperties()));
        }

        experimentService.deleteExperiment(expConfig);

        return idExp;
    }

    @RequestMapping(value="/user/experiment/runList", method=RequestMethod.GET, params="loadExperimentButton")
    public String loadExperiment(Model model,
                                 @RequestParam("runId") String runId) {

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

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

        model.addAttribute("configuration", configExpDto);
        model.addAttribute("configExp", configExpDto);
        model.addAttribute("runList", runList);
        model.addAttribute("dataTypeList", run.getExperimentId().getIdExpDataTypeList());

        return "/user/experiment/configExperiment";
    }

    @GetMapping(value="/user/experiment/runList", params="runExperimentButton")
    public String runExperiment(Model model,
                          @RequestParam(value = "runId") String runId) throws IOException {

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        Long longRunId = Long.parseLong(runId);
        Run run = runService.findByRunId(longRunId);
        DiagramData diagramData = diagramDataService.findByRunId(run);

        ExperimentDetailsDto experimentDetailsDto = new ExperimentDetailsDto();

        experimentDetailsDto.setExperimentId(run.getExperimentId().getId());
        experimentDetailsDto.setExperimentName(run.getExperimentId().getExperimentName());
        experimentDetailsDto.setExperimentDescription(run.getExperimentId().getExperimentDescription());

        experimentDetailsDto.setRunId(run.getId());

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

        if(run.getStatus().equals(Run.Status.RUNNING)){      // We don't execute it if it's RUNNING
            model.addAttribute("expDetails", experimentDetailsDto);
            return "/user/experiment/experimentDetails";
        }
        experimentDetailsDto.setStatus(run.getStatus());
        model.addAttribute("expDetails", experimentDetailsDto);

        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        run.setModificationDate(currentTimestamp);

        Iterator<DiagramPair> it = diagramData.getListPair().iterator();
        ArrayList<DiagramPair> lAux = new ArrayList<>();
        while(it.hasNext()){
            DiagramPair value = it.next();
            lAux.add(value);
        }
        diagramData.getListPair().removeAll(lAux);
        diagramDataService.saveDiagram(diagramData);
        runExperimentDetails(user, run, run.getDiagramData());

        return "/user/experiment/experimentDetails";
    }

    @GetMapping(value="/user/experiment/runList", params="showPlotExecutionButton")
    public String showPlotExecutionExperiment(Model model,
                                @RequestParam(value = "runId") String runId) {

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        Long longRunId = Long.parseLong(runId);
        Run run = runService.findByRunId(longRunId);
        DiagramData diagramData = diagramDataService.findByRunId(run);

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

        model.addAttribute("expDetails", experimentDetailsDto);

        return "/user/experiment/showDiagramPlot";
    }

    public void runExperimentDetails(User user, Run run, DiagramData diagramData) throws IOException {
        ExpProperties prop = experimentService.findPropertiesById(run.getIdProperties());
        String propertiesFilePath = PROPERTIES_DIR_PATH + user.getId() + File.separator + run.getExperimentName().replaceAll("\\s+","") + "_" + prop.getUuidPropDto() + ".properties";

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
        th.start();
        threadMap.put(th.getId(), th);
        run.setThreaId(th.getId());
        runnables.put(th.getId(),obj);
        // https://stackoverflow.com/questions/26213615/terminating-thread-using-thread-id-in-java

        propertiesReader.close();
    }

    public void createPropertiesFile(String propertiesFilePath, ExpPropertiesDto propertiesDto, String expName, java.sql.Timestamp currentTimeStamp) throws IOException {
        File propertiesNewFile = new File(propertiesFilePath);
        if (!propertiesNewFile.exists()) {
            propertiesNewFile.createNewFile();
        }

        PrintWriter propertiesWriter = new PrintWriter(propertiesNewFile);

        propertiesWriter.println("# ExpPropertiesDto for " +  expName);
        propertiesWriter.println("# " + currentTimeStamp.toString());
        propertiesWriter.println("LoggerBasePath=" +  propertiesDto.getLoggerBasePath().replace("\\", "/"));
        propertiesWriter.println("ErrorThreshold=" +  propertiesDto.getErrorThreshold());
        propertiesWriter.println("TournamentSize=" + propertiesDto.getTournamentSize());
        propertiesWriter.println("WorkDir=" + propertiesDto.getWorkDir().replace("\\", "/"));
        propertiesWriter.println("RealDataCopied=" + propertiesDto.getRealDataCopied());
        propertiesWriter.println("CrossoverProb=" + propertiesDto.getCrossoverProb());
        propertiesWriter.println("BnfPathFile=" + propertiesDto.getBnfPathFile().substring(2, propertiesDto.getBnfPathFile().length()).replace("\\", "/"));
        propertiesWriter.println("Objectives=" +  propertiesDto.getObjectives());
        propertiesWriter.println("ClassPathSeparator=" + propertiesDto.getClassPathSeparator());
        propertiesWriter.println("Executions=" +  propertiesDto.getExecutions());
        propertiesWriter.println("LoggerLevel=" + propertiesDto.getLoggerLevel());
        propertiesWriter.println("MutationProb=" +  propertiesDto.getMutationProb());
        propertiesWriter.println("NormalizeData=" + propertiesDto.getNormalizedData());
        propertiesWriter.println("LogPopulation=" +  propertiesDto.getLogPopulation());
        propertiesWriter.println("ChromosomeLength=" + propertiesDto.getChromosomeLength());
        propertiesWriter.println("NumIndividuals=" +  propertiesDto.getNumIndividuals());
        propertiesWriter.println("NumGenerations=" + propertiesDto.getNumGenerations());
        propertiesWriter.println("ViewResults=" +  propertiesDto.getViewResults());
        propertiesWriter.println("MaxWraps=" + propertiesDto.getMaxWraps());
        propertiesWriter.println("ModelWidth=" + propertiesDto.getModelWidth());

        if(propertiesDto.getTraining())
            propertiesWriter.println("TrainingPath=" + propertiesDto.getTrainingPath().substring(2, propertiesDto.getTrainingPath().length()).replace("\\", "/"));
        else if(propertiesDto.getValidation()) {
            propertiesWriter.println("TrainingPath=" + propertiesDto.getTrainingPath().substring(2, propertiesDto.getTrainingPath().length()).replace("\\", "/")); // TEMPORAL UNTIL KNOW IF WE NEED THIS OR NOT
            propertiesWriter.println("ValidationPath=" + propertiesDto.getValidationPath().substring(2, propertiesDto.getValidationPath().length()).replace("\\", "/"));
        }
        else {
            propertiesWriter.println("TrainingPath=" + propertiesDto.getTrainingPath().substring(2, propertiesDto.getTrainingPath().length()).replace("\\", "/"));
            propertiesWriter.println("TestPath=" + propertiesDto.getTestPath().substring(2, propertiesDto.getTestPath().length()).replace("\\", "/"));
                                                                                   // ("\\", "/")
        }
        propertiesWriter.close();
    }

    public ExpProperties createExpPropertiesEntity(ExpProperties expProp, Properties properties, Experiment experiment, Run run, ExpPropertiesDto propDto, String dataFilePath){
        expProp.setUuidPropDto(propDto.getId().toString());

        expProp.setIdExp(experiment.getId());
        expProp.setIdRun(run.getId());

        run.setIdProperties(expProp.getId());

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

    public Grammar grammarSection(Grammar grammar, GrammarDto grammarDto){
        grammar.setGrammarName(grammarDto.getGrammarName());
        grammar.setGrammarDescription(grammarDto.getGrammarDescription());
        grammar.setFileText(grammarDto.getFileText());

        return grammar;
    }

    public Run runSection(Run run, Grammar grammar, ConfigExperimentDto configExpDto, java.sql.Timestamp currentTimestamp){
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

        String grammarFilePath = GRAMMAR_DIR_PATH + user.getId() + File.separator + configExpDto.getExperimentName().replaceAll("\\s+","") + "_" + grammar.getId() + ".bnf";

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
        if(! fileModelDto.getTypeFile().isEmpty()){
            expDataType.setDataTypeName(expDataTypeDto.getDataTypeName());
            expDataType.setDataTypeDescription(expDataTypeDto.getDataTypeDescription());
            expDataType.setCreationDate(currentTimestamp);
            expDataType.setDataTypeType("training");
        }
        else if (expDataType.getRunId() != null) {
            expDataType.setModificationDate(currentTimestamp);
            expDataType.setDataTypeType(expDataTypeDto.getDataTypeType().toString());
        }

        return expDataType;
    }

    public Experiment experimentSection(Experiment exp, User user, ExperimentDataType expDataType, ConfigExperimentDto configExpDto, Grammar grammar, Run run, java.sql.Timestamp currentTimestamp, Long longDefaultRunId){
        if(exp == null) {   // We create it
            exp = new Experiment(user, configExpDto.getExperimentName(), configExpDto.getExperimentDescription() ,configExpDto.getGenerations(),
                    configExpDto.getPopulationSize(), configExpDto.getMaxWraps(), configExpDto.getTournament(), configExpDto.getCrossoverProb(), configExpDto.getMutationProb(),
                    configExpDto.getInitialization(), configExpDto.getResults(), configExpDto.getNumCodons(), configExpDto.getNumberRuns(), configExpDto.getObjective() ,currentTimestamp, currentTimestamp);
            exp.setDefaultRunId(longDefaultRunId);          // Doesn't exists -> We set up the run id obtained before
            user.getUserDetails().addExperiment(exp);       // We add it only if doesn't exist

        }
        else {  // The experiment data type configuration already exist
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

            exp.setDefaultRunId(run.getId());          // Doesn't exists -> We set up the run id obtained before
        }

        exp.addRun(run);
        exp.addGrammar(grammar);
        exp.addExperimentDataType(expDataType);

        exp.setDefaultGrammar(grammar.getId());
        exp.setDefaultExpDataType(expDataType.getId());

        return exp;
    }

    @GetMapping("/user/experiment/experimentDetails")
    public String experimentDetails(@ModelAttribute("expDetails") ExperimentDetailsDto expDetailsDto){
        return "/user/experiment/experimentDetails";
    }

    @PostMapping(value="/user/experiment/stopRun", params="stopRunExperimentButton")
    public String stopRunExperiment(Model model,
                                    @RequestParam("runIdStop") String runIdStop){
        Run run = runService.findByRunId(Long.parseLong(runIdStop));
        Long threadId = run.getThreaId();

        // https://stackoverflow.com/questions/26213615/terminating-thread-using-thread-id-in-java
        Thread th = threadMap.get(threadId);
        th.interrupt();

        runnables.get(threadId).stopExecution();

        run.setStatus(Run.Status.STOPPED);

        ExperimentDetailsDto experimentDetailsDto = new ExperimentDetailsDto();

        experimentDetailsDto.setExperimentId(run.getExperimentId().getId());
        experimentDetailsDto.setExperimentName(run.getExperimentId().getExperimentName());
        experimentDetailsDto.setExperimentDescription(run.getExperimentId().getExperimentDescription());

        experimentDetailsDto.setRunId(run.getId());

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

        experimentDetailsDto.setBestIndividual(run.getDiagramData().getBestIndividual());
        experimentDetailsDto.setCurrentGeneration(run.getDiagramData().getCurrentGeneration());

        experimentDetailsDto.setStatus(run.getStatus());

        model.addAttribute("expDetails", experimentDetailsDto);
        return "/user/experiment/showDiagramPlot";
    }

    @RequestMapping(value="/user/experiment/expRepoSelected", method=RequestMethod.POST, params="deleteRun")
    public
    @ResponseBody Long deleteRun(@RequestParam("runId") String runId){
        Boolean found = false;

        Long longRunId = Long.parseLong(runId);
        Run run = runService.findByRunId(longRunId);
        Experiment experiment = run.getExperimentId();

        List<Grammar> lGrammar = experiment.getIdGrammarList();
        Iterator<Grammar> grammarIt = lGrammar.iterator();
        while(grammarIt.hasNext() && !found) {
            Grammar grammarAux = grammarIt.next();
            if (grammarAux.getId() == run.getDefaultGrammarId()) {
                experimentService.deleteGrammar(grammarAux);

                if(grammarAux.getId() == experiment.getDefaultGrammar())
                    experiment.setDefaultRunId(null);
                found = true;
            }
        }
        found = false;

        List<ExperimentDataType> lExpDataType = experiment.getIdExpDataTypeList();
        Iterator<ExperimentDataType> expDataIt = lExpDataType.iterator();
        while(expDataIt.hasNext() && !found) {
            ExperimentDataType expDataAux = expDataIt.next();
            if (expDataAux.getId() == run.getDefaultExpDataTypeId()) {
                experimentService.deleteDataTypeFile(expDataAux);

                if(expDataAux.getId() == experiment.getDefaultExpDataType())
                    experiment.setIdExpDataTypeList(null);
                found = true;
            }
        }
        found = false;

        List<Run> lRun = run.getExperimentId().getIdRunList();
        Iterator<Run> runIt = lRun.iterator();
        while(runIt.hasNext() && !found){
            Run runAux = runIt.next();
            if(runAux.getId() == run.getId()) {
                runAux.setExperimentId(null);

                if(runAux.getId() == experiment.getDefaultRunId())
                    experiment.setDefaultRunId(Long.parseLong("0"));
                found = true;
            }
        }

        experimentService.saveExperiment(experiment);
        experimentService.updateExperiment();
        runService.deleteExpProperties(experimentService.findPropertiesById(run.getIdProperties()));

        return longRunId;
    }

    public ConfigExperimentDto fillConfigExpDto(ConfigExperimentDto configExpDto, Experiment exp, Run run, Grammar grammar, ExperimentDataType expDataType){
        if(run == null){
            configExpDto.setDefaultRunId(exp.getDefaultRunId());
            configExpDto.setExperimentName(exp.getExperimentName());
            configExpDto.setExperimentDescription(exp.getExperimentDescription());
            configExpDto.setCrossoverProb(exp.getCrossoverProb());
            configExpDto.setGenerations(exp.getGenerations());
            configExpDto.setPopulationSize(exp.getPopulationSize());
            configExpDto.setMaxWraps(exp.getMaxWraps());
            configExpDto.setTournament(exp.getTournament());
            configExpDto.setCrossoverProb(exp.getCrossoverProb());
            configExpDto.setMutationProb(exp.getMutationProb());
            configExpDto.setInitialization(exp.getInitialization());
            configExpDto.setResults(exp.getResults());
            configExpDto.setNumCodons(exp.getNumCodons());
            configExpDto.setNumberRuns(exp.getNumberRuns());
            configExpDto.setObjective(exp.getObjective());
        }
        else {
            configExpDto.setDefaultRunId(run.getId());
            configExpDto.setExperimentName(run.getExperimentName());
            configExpDto.setExperimentDescription(run.getExperimentDescription());
            configExpDto.setCrossoverProb(run.getCrossoverProb());
            configExpDto.setGenerations(run.getGenerations());
            configExpDto.setPopulationSize(run.getPopulationSize());
            configExpDto.setMaxWraps(run.getMaxWraps());
            configExpDto.setTournament(run.getTournament());
            configExpDto.setCrossoverProb(run.getCrossoverProb());
            configExpDto.setMutationProb(run.getMutationProb());
            configExpDto.setInitialization(run.getInitialization());
            configExpDto.setResults(run.getResults());
            configExpDto.setNumCodons(run.getNumCodons());
            configExpDto.setNumberRuns(run.getNumberRuns());
            configExpDto.setObjective(run.getObjective());
        }

        configExpDto.setId(exp.getId());
        configExpDto.setDefaultExpDataTypeId(exp.getDefaultExpDataType());
        configExpDto.setDefaultGrammarId(exp.getDefaultGrammar());

        configExpDto.setGrammarName(grammar.getGrammarName());
        configExpDto.setGrammarDescription(grammar.getGrammarDescription());
        configExpDto.setFileText(grammar.getFileText());

        configExpDto.setDataTypeName(expDataType.getDataTypeName());
        configExpDto.setDataTypeDescription(expDataType.getDataTypeDescription());

        return configExpDto;
    }
}