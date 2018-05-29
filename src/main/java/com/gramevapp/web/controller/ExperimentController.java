package com.gramevapp.web.controller;

import com.engine.algorithm.SymbolicRegressionGE;
import com.gramevapp.web.model.*;
import com.gramevapp.web.service.DiagramDataService;
import com.gramevapp.web.service.ExperimentService;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import com.engine.algorithm.RunGeObserver;
import javafx.util.converter.TimeStringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.Valid;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import static com.engine.util.Common.OBJECTIVES_PROP;
import static com.engine.util.Common.TRAINING_PATH_PROP;

@Controller
public class ExperimentController {

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    @Autowired
    private RunService runService;

    @ModelAttribute
    public FileModelDto fileModel(){
        return new FileModelDto();
    }

    private final String GRAMMAR_DIR_PATH = "." + File.separator + "resources" + File.separator + "files" + File.separator + "grammar" + File.separator + "";
    private final String DATATYPE_DIR_PATH = "." + File.separator + "resources" + File.separator + "files" + File.separator + "dataType" + File.separator + "";
    private final String PROPERTIES_DIR_PATH = "." + File.separator + "resources" + File.separator + "files" + File.separator + "properties" + File.separator + "";

    @GetMapping("/user/experiment/configExperiment")
    public String configExperiment(Model model,
                                   @ModelAttribute("configuration") ExperimentDto expDto){


        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        // WE NEED TO ADD HERE THE EXPERIMENT INFO TO SEND IT TO configExperiment
        Experiment expConfig = experimentService.findExperimentById(expDto.getId());
        Grammar grammarDto;
        ExperimentDataType expDataTypeDto;
        List<Run> runList;
        List<ExperimentDataType> expDataTypeList;
        List<Grammar> expGrammarList;
        ConfigExperimentDto confExpDto = new ConfigExperimentDto();

        if(expConfig != null) {
            grammarDto = expConfig.getDefaultGrammar();
            expDataTypeDto = expConfig.getDefaultExpDataType();

            //List<Run> runList = runService.findAllByUserId(user);
            runList = expConfig.getIdRunList();
            expDataTypeList = expConfig.getIdExpDataTypeList();
            expGrammarList = expConfig.getIdGrammarList();
        }
        else {
            expConfig = new Experiment();
            grammarDto = new Grammar();
            expDataTypeDto = new ExperimentDataType();

            runList = new ArrayList();
            expDataTypeList = new ArrayList();
            expGrammarList = new ArrayList();
        }

        model.addAttribute("grammar", grammarDto);
        model.addAttribute("type", expDataTypeDto);
        model.addAttribute("configuration", expConfig);
        model.addAttribute("runList", runList);
        model.addAttribute("dataTypeList", expDataTypeList);
        model.addAttribute("grammarList", expGrammarList);
        model.addAttribute("user", user);
        model.addAttribute("configExp", confExpDto);

        return "/user/experiment/configExperiment";
    }

    // Update the parameters of the experiment, do not create a new one
    @RequestMapping(value="/user/experiment/start", method=RequestMethod.POST, params="saveExperimentButton")
    public String saveExperiment(@ModelAttribute("grammar") GrammarDto grammarDto,
                                 @ModelAttribute("type") ExperimentDataTypeDto expDataTypeDto,
                                 @ModelAttribute("configuration") ExperimentDto expDto,
                                 BindingResult result) throws IllegalStateException {

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            return "/user/experiment/configExperiment";
        }

        Experiment updExp = experimentService.findExperimentById(expDto.getId());
        if(updExp == null) // Do not update nothing
            return "redirect:/user/experiment/configExperiment";
        else{

            // Grammar section
            Grammar grammar = new Grammar(user, grammarDto.getGrammarName(), grammarDto.getGrammarDescription(), grammarDto.getFileText());
            // END - Grammar section

            // DataType section
            ExperimentDataType expDataType =  new ExperimentDataType(user, expDataTypeDto.getDataTypeName(), expDataTypeDto.getDataTypeDescription(), expDataTypeDto.getDataTypeType(), updExp.getDefaultExpDataType().getCreationDate(),updExp.getDefaultExpDataType().getModificationDate());
            // END - DataType section

            // DATE TIMESTAMP
            Calendar calendar = Calendar.getInstance();
            java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
            // END - DATE TIMESTAMP

            updExp.updateExperiment(grammar, expDataType, expDto.getExperimentName(), expDto.getExperimentDescription() ,expDto.getGenerations(),
                    expDto.getPopulationSize(), expDto.getMaxWraps(), expDto.getTournament(), expDto.getCrossoverProb(), expDto.getMutationProb(),
                    expDto.getInitialization(), expDto.getResults(), expDto.getNumCodons(), expDto.getNumberRuns(), currentTimestamp);

            return "redirect:/user/experiment/configExperiment";
        }

    }

    @RequestMapping(value="/user/experiment/start", method=RequestMethod.POST, params="cloneExperimentButton")
    public String cloneExperiment(@ModelAttribute("configExperiment") @Valid ExperimentDto experimentDto) {
        return "/user/experiment/configExperiment";}

    @RequestMapping(value="/user/experiment/start", method=RequestMethod.POST, params="runExperimentButton")
    public String runExperiment(Model model,
                                @ModelAttribute("grammar") GrammarDto grammarDto,
                                @ModelAttribute("type") ExperimentDataTypeDto expDataTypeDto,
                                @ModelAttribute("configuration") ExperimentDto expDto,
                                @RequestParam String radioDataType,
                                @ModelAttribute("typeFile") FileModelDto fileModelDto,
                                @ModelAttribute("configExp") @Valid ConfigExperimentDto configExperimentDto,
                                BindingResult result) throws IllegalStateException, IOException {

        if (result.hasErrors()){
            return "/user/experiment/configExperiment";
        }

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        // CONFIGURATION SECTION

        // GRAMMAR SECTION
        Grammar grammar = grammarSection(user, grammarDto);
        // END - GRAMMAR SECTION

        // DATE TIMESTAMP
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        // END - DATE TIMESTAMP

        // Experiment Data Type SECTION
        ExperimentDataType expDataType = experimentDataTypeSection(user, expDataTypeDto, currentTimestamp);
        // END - Experiment Data Type SECTION

        // RUN SECTION
        Run run = new Run(user, Run.Status.INITIALIZING, expDto.getExperimentName(), expDto.getExperimentDescription(), currentTimestamp, currentTimestamp);
        Long longDefaultRunId = runService.saveRun(run).getId();
        // END - RUN SECTION

        // Experiment section
        Experiment exp = experimentSection(user, expDto, grammar, expDataType, run, currentTimestamp, longDefaultRunId);
        // END - Experiment section

        /** We need save first the expDataType rather than expRowType, because if we did otherwise we will have an detached error
         *  this means that we are trying to access to an entity that doesn't exist yet (Because in expRowType in add method
         *  we are adding the row to expDataType. And this isn't created yet.
         **/
        experimentService.saveDataType(expDataType);

        // Grammar File SECTION
        String grammarFilePath = grammarFileSection(user, expDto, grammar);
        // END - Grammar File SECTION

        // Create PropertiesDto file
        PropertiesDto propertiesDto = new PropertiesDto(0.0, expDto.getTournament(), 0, expDto.getCrossoverProb(), grammarFilePath, 0, 1, expDto.getMutationProb(), false, 1, expDto.getNumCodons(), expDto.getPopulationSize(), expDto.getGenerations(), false, expDto.getMaxWraps(), 500, expDto.getExperimentName(), expDto.getExperimentDescription());

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
        else {      // Training
            dataTypeDirectoryPath += "training\\" + user.getId();
            propertiesDto.setTrainingPath(dataTypeDirectoryPath + File.separator + expDto.getExperimentName() + "_" + expDataType.getId() + ".csv");   // TEMPORAL UNTIL KNOW IF WE NEED THIS OR NOT
            propertiesDto.setTraining(true);
        }

        File dir = new File(PROPERTIES_DIR_PATH + user.getId());
        if (!dir.exists())
            dir.mkdirs();

        String propertiesFilePath = PROPERTIES_DIR_PATH + user.getId() + File.separator + expDto.getExperimentName() + "_" + propertiesDto.getId() + ".properties";

        createPropertiesFile(propertiesFilePath, propertiesDto, expDto.getExperimentName(), currentTimestamp);  // Write in property file
        // END - Create PropertiesDto file

        // MultipartFile section
        MultipartFile multipartFile = fileModelDto.getTypeFile();

        String dataFilePath = "";

        // If Radio button and file path selected -> File path is selected
        // NULL -> didn't select the dataType file from the list - ON if th:value in input is empty
        if( (radioDataType.equals("on") && !multipartFile.isEmpty()) || (!radioDataType.equals("on") && !multipartFile.isEmpty()) ) {
            File tmpFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") +
                    multipartFile.getOriginalFilename());
            multipartFile.transferTo(tmpFile);

            dataFilePath = tmpFile.getAbsolutePath();

            Reader reader = new FileReader(tmpFile);
            experimentService.loadExperimentRowTypeFile(reader, expDataType);   // Save row here
            reader.close();
        }
        else if(radioDataType.equals("on") && multipartFile.isEmpty()) {        // Radio button neither file path selected
            result.rejectValue("typeFile", "error.typeFile", "Choose one file");
            return "/user/experiment/configExperiment";
        }
        else{   // DataType selected from list
            // We won't save it, because already exists. But we are going to use it in our execution
            Long idDataType = Long.parseLong(radioDataType);

            ExperimentDataType expDataTypeSelected = experimentService.findExperimentDataTypeById(idDataType);

            List<ExperimentRowType> lExpRowType = expDataTypeSelected.getListRowsFile();

            // Create temporal training path file
            File tmpFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") +
                    "trainingPathFile.csv");

            tmpFile.createNewFile();

            FileWriter fWriter = new FileWriter(tmpFile, false);    // true = append; false = overwrite
            BufferedWriter writer = new BufferedWriter(fWriter);

            for(ExperimentRowType e : lExpRowType){
                if(e.getYCustom()!=null)
                    writer.append(e.toString());
            }
            writer.close();

            dataFilePath = tmpFile.getAbsolutePath();
        }
        // END Reader - FILE DATA TYPE
        // END - Multipart File Section

        experimentService.saveGrammar(grammar);


        // END CONFIGURATION SECTION

        expDto.setDefaultRunId(run.getId());
        expDto.setId(exp.getId());

        model.addAttribute("configuration", expDto);
        model.addAttribute("grammar", grammar);
        model.addAttribute("type", expDataType);
        model.addAttribute("grammarList", exp.getIdGrammarList());
        model.addAttribute("dataTypeList", exp.getIdExpDataTypeList());
        model.addAttribute("runList", exp.getIdRunList());

        // Execute program with experiment info
        File propertiesFile = new File(propertiesFilePath);
        Reader propertiesReader = new FileReader(propertiesFile);

        Properties properties = new Properties();
        properties.load(propertiesReader);

        // TODO: no distinguir el tipo de fichero entre training, validation o test.
        properties.setProperty(TRAINING_PATH_PROP, dataFilePath);

        DiagramData diagramData = new DiagramData(run.getId(), user.getId());
        diagramData.setTime(currentTimestamp);

        executeGramEv(properties, diagramData);        // PropertiesDto properties, int threadId, int numObjectives

        propertiesReader.close();
        // END - Execute program with experiment info

        return "/user/experiment/configExperiment";
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

    @RequestMapping(value="/user/experiment/expRepoSelected")
    public String expRepoSelected(Model model,
                                  @RequestParam(required=false) String id ){ // Exp ID

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        if(id == null)
            return "redirect:/user/experiment/experimentRepository";

        Long idExp = Long.parseLong(id);

        Experiment expConfig = experimentService.findExperimentByUserIdAndExpId(user, idExp);

        Grammar grammar = expConfig.getDefaultGrammar();
        ExperimentDataType expDataType = expConfig.getDefaultExpDataType();
        List<Run> runList = expConfig.getIdRunList();
        ConfigExperimentDto confExpDto = new ConfigExperimentDto();

        model.addAttribute("configuration", expConfig);
        model.addAttribute("grammar", grammar);
        model.addAttribute("type", expDataType);
        model.addAttribute("runList", runList);
        model.addAttribute("grammarList", expConfig.getIdGrammarList());
        model.addAttribute("dataTypeList", expConfig.getIdExpDataTypeList());
        model.addAttribute("configExp", confExpDto);

        return "/user/experiment/configExperiment";
    }

    @RequestMapping(value="/user/experiment/runList", method=RequestMethod.GET, params="loadExperimentButton")
    public String runList(Model model,
                          @RequestParam(value = "runId") String id) {

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        Long longRunId = Long.parseLong(id);
        Run run = runService.findByUserIdAndRunId(user, longRunId);
        Experiment expConfig = run.getExperimentId();
        Grammar grammar = expConfig.getDefaultGrammar();
        ExperimentDataType expDataType = expConfig.getDefaultExpDataType();
        List<Run> runList = expConfig.getIdRunList();
        ConfigExperimentDto confExpDto = new ConfigExperimentDto();

        expConfig.setDefaultRunId(longRunId);   // We set up the default run id to the experiment, this way we know what run to load

        model.addAttribute("configuration", expConfig);
        model.addAttribute("grammar", grammar);
        model.addAttribute("type", expDataType);
        model.addAttribute("runList", runList);
        model.addAttribute("grammarList", expConfig.getIdGrammarList());
        model.addAttribute("dataTypeList", expConfig.getIdExpDataTypeList());
        model.addAttribute("configExp", confExpDto);

        return "/user/experiment/configExperiment";
    }

    //  Run gramEv application
    public void executeGramEv(Properties properties, DiagramData diagramData) throws IOException {

        int numObjectives = 1;
        if ((properties.getProperty(OBJECTIVES_PROP) != null)
                && (Integer.valueOf(properties.getProperty(OBJECTIVES_PROP)) == 2)) {
            numObjectives = 2;
        }

        SymbolicRegressionGE ge = new SymbolicRegressionGE(properties,numObjectives);

        RunGeObserver observer = new RunGeObserver();
        observer.setDiagramData(diagramData);

        ge.runGE(observer);
    }


    public void createPropertiesFile(String propertiesFilePath, PropertiesDto propertiesDto, String expName, java.sql.Timestamp currentTimeStamp) throws IOException {
        File propertiesNewFile = new File(propertiesFilePath);
        if (!propertiesNewFile.exists()) {
            propertiesNewFile.createNewFile();
        }

        PrintWriter propertiesWriter = new PrintWriter(propertiesNewFile);

        propertiesWriter.println("# PropertiesDto for " +  expName);
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

    public Grammar grammarSection(User user, GrammarDto grammarDto){
        Grammar grammar = experimentService.findGrammarByUserIdAndName(user, grammarDto.getGrammarName());

        if(grammar == null)     // We create it
            grammar = new Grammar(user, grammarDto.getGrammarName(), grammarDto.getGrammarDescription(), grammarDto.getFileText());
        else {  // The grammar already exist
            grammar.setGrammarName(grammarDto.getGrammarName());
            grammar.setGrammarDescription(grammarDto.getGrammarDescription());
            grammar.setFileText(grammarDto.getFileText());
        }

        return grammar;
    }

    public String grammarFileSection(User user, ExperimentDto expDto, Grammar grammar) throws IllegalStateException, IOException {

        File dir = new File(GRAMMAR_DIR_PATH + user.getId());
        if (!dir.exists())
            dir.mkdirs();

        String grammarFilePath = GRAMMAR_DIR_PATH + user.getId() + File.separator + expDto.getExperimentName() + "_" + grammar.getId() + ".bnf";

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

    public ExperimentDataType experimentDataTypeSection(User user, ExperimentDataTypeDto expDataTypeDto, java.sql.Timestamp currentTimestamp) throws IOException {
        ExperimentDataType expDataType = experimentService.findDataTypeByUserIdAndName(user, expDataTypeDto.getDataTypeName());

        if(expDataType == null)     // We create it
            expDataType = new ExperimentDataType(user, expDataTypeDto.getDataTypeName(), expDataTypeDto.getDataTypeDescription(), "training", currentTimestamp, currentTimestamp);
        else {  // The experiment data type configuration already exist
            expDataType.setDataTypeName(expDataTypeDto.getDataTypeName());
            expDataType.setDataTypeDescription(expDataTypeDto.getDataTypeDescription());
            expDataType.setDataTypeType(expDataType.getDataTypeType());
            // We could update the date time if we would like
        }

        return expDataType;
    }

    public Experiment experimentSection(User user, ExperimentDto expDto, Grammar grammar, ExperimentDataType expDataType, Run run, java.sql.Timestamp currentTimestamp, Long longDefaultRunId){
        Experiment exp = experimentService.findExperimentByUserIdAndExpId(user, expDto.getId());

        if(exp == null) {   // We create it
            exp = new Experiment(user, expDto.getExperimentName(), expDto.getExperimentDescription() ,expDto.getGenerations(),
                    expDto.getPopulationSize(), expDto.getMaxWraps(), expDto.getTournament(), expDto.getCrossoverProb(), expDto.getMutationProb(),
                    expDto.getInitialization(), expDto.getResults(), expDto.getNumCodons(), expDto.getNumberRuns(), expDto.getObjective() ,currentTimestamp, currentTimestamp);

            exp.addGrammar(grammar);
            exp.addExperimentDataType(expDataType);
            exp.addRun(run);

            exp.setDefaultGrammar(grammar);
            exp.setDefaultExpDataType(expDataType);
            exp.setDefaultRunId(longDefaultRunId);          // Doesn't exists -> We set up the run id obtained before
        }
        else {  // The experiment data type configuration already exist
            exp.setExperimentName(expDto.getExperimentName());
            exp.setExperimentDescription(expDto.getExperimentDescription());
            exp.setGenerations(expDto.getGenerations());
            exp.setPopulationSize(expDto.getPopulationSize());
            exp.setMaxWraps(expDto.getMaxWraps());
            exp.setTournament(expDto.getTournament());
            exp.setCrossoverProb(expDto.getCrossoverProb());
            exp.setMutationProb(expDto.getMutationProb());
            exp.setInitialization(expDto.getInitialization());
            exp.setResults(expDto.getResults());
            exp.setNumCodons(expDto.getNumCodons());
            exp.setNumberRuns(expDto.getNumberRuns());
            exp.setObjective(expDto.getObjective());

            exp.addGrammar(grammar);
            exp.addExperimentDataType(expDataType);
            exp.addRun(run);

            exp.setDefaultGrammar(grammar);
            exp.setDefaultExpDataType(expDataType);
            exp.setDefaultRunId(run.getId());           // Already exist, so we get it from the RUN model
        }

        user.addExperiment(exp);

        return exp;
    }

}