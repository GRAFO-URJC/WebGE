package com.gramevapp.web.controller;

import com.gramevapp.web.model.*;
import com.gramevapp.web.service.ExperimentService;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import engine.algorithm.GramEvalTemporalModel;
import jeco.core.algorithm.moge.AbstractProblemGE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

@Controller
public class ExperimentController {

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    @Autowired
    private RunService runService;

    @GetMapping("/user/experiment/configExperiment")
    public String configExperiment(Model model,
                                   @ModelAttribute("configuration") ExperimentDto expDto){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {    // User not authenticated
            System.out.println("User not authenticated");
            return "redirect:/login";
        }
        User user = userService.findByEmail(authentication.getName());

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
                                 BindingResult result) throws IllegalStateException, IOException  {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {    // User not authenticated
            System.out.println("User not authenticated");
            return "redirect:/login";
        }
        User user = userService.findByEmail(authentication.getName());

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
                                @ModelAttribute("configuration")  ExperimentDto expDto,
                                @RequestParam String radioDataType,
                                @RequestParam("typeFile") MultipartFile multipartFile,
                                @Valid @ModelAttribute("configExp") ConfigExperimentDto configExperimentDto)  throws IllegalStateException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {    // User not authenticated
            System.out.println("User not authenticated");
            return "redirect:/login";
        }
        User user = userService.findByEmail(authentication.getName());

        // CONFIGURATION SECTION
        Grammar grammar = experimentService.findGrammarByUserIdAndName(user, grammarDto.getGrammarName());

        if(grammar == null)     // We create it
            grammar = new Grammar(user, grammarDto.getGrammarName(), grammarDto.getGrammarDescription(), grammarDto.getFileText());
        else {  // The grammar already exist
            grammar.setGrammarName(grammarDto.getGrammarName());
            grammar.setGrammarDescription(grammarDto.getGrammarDescription());
            grammar.setFileText(grammarDto.getFileText());
        }

        // DATE TIMESTAMP
        Calendar calendar = Calendar.getInstance();
        java.sql.Date currentTimestamp = new java.sql.Date(calendar.getTime().getTime());
        // END - DATE TIMESTAMP

        // Experiment Data Type Section
        ExperimentDataType expDataType = experimentService.findDataTypeByUserIdAndName(user, expDataTypeDto.getDataTypeName());

        if(expDataType == null)     // We create it
            expDataType = new ExperimentDataType(user, expDataTypeDto.getDataTypeName(), expDataTypeDto.getDataTypeDescription(), expDataTypeDto.getDataTypeType(), currentTimestamp, currentTimestamp);
        else {  // The experiment data type configuration already exist
            expDataType.setDataTypeName(expDataTypeDto.getDataTypeName());
            expDataType.setDataTypeDescription(expDataTypeDto.getDataTypeDescription());
            expDataType.setDataTypeType(expDataType.getDataTypeType());
            // We could update the date time if we would like
        }
        // END - Experiment Data Type Section

        // RUN SECTION
        Run run = new Run(user, Run.Status.INITIALIZING, expDto.getExperimentName(), expDto.getExperimentDescription(), currentTimestamp, currentTimestamp);
        // END RUN SECTION

        // Experiment section
        Experiment exp = experimentService.findExperimentByUserIdAndName(user, expDto.getExperimentName());

        if(exp == null) {     // We create it
            exp = new Experiment(user, expDto.getExperimentName(), expDto.getExperimentDescription() ,expDto.getGenerations(),
                    expDto.getPopulationSize(), expDto.getMaxWraps(), expDto.getTournament(), expDto.getCrossoverProb(), expDto.getMutationProb(),
                    expDto.getInitialization(), expDto.getResults(), expDto.getNumCodons(), expDto.getNumberRuns(), currentTimestamp, currentTimestamp);

            exp.addGrammar(grammar);
            exp.addExperimentDataType(expDataType);
            exp.addRun(run);

            exp.setDefaultGrammar(grammar);
            exp.setDefaultExpDataType(expDataType);
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

            exp.addGrammar(grammar);
            exp.addExperimentDataType(expDataType);
            exp.addRun(run);

            exp.setDefaultGrammar(grammar);
            exp.setDefaultExpDataType(expDataType);
        }

        user.addExperiment(exp);
        // END - Experiment section

        // SAVE Experiment
        //experimentService.saveExperiment(exp);

        /** We need save first the expDataType rather than expRowType, because if we did otherwise we will have an detached error
         *  this means that we are trying to access to an entity that dont exist already (Because in expRowType in add method
         *  we are adding the row to expDataType. And this isn't created yet.
         **/
        experimentService.saveDataType(expDataType);

        // Grammar File
        new File(".\\resources\\files\\grammar\\" + user.getId()).mkdirs(); // Create the directory to save datatype files
        String grammarFilePath = ".\\resources\\files\\grammar\\" + user.getId() + "\\" + expDto.getExperimentName().replaceAll("\\s+", "") + grammar.getGrammarName().replaceAll("\\s+","") + ".bnf";

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
        // END Grammar File

        // Create PropertiesDto file
        PropertiesDto propertiesDto = new PropertiesDto(0.0, expDto.getTournament(), 0, expDto.getCrossoverProb(), grammarFilePath, 0, 1, expDto.getMutationProb(), false, 1, expDto.getNumCodons(), expDto.getPopulationSize(), expDto.getGenerations(), false, expDto.getMaxWraps(), 500, expDto.getExperimentName(), expDto.getExperimentDescription());

        // Reader - FILE DATA TYPE - Convert MultipartFile into Generic Java File - Then convert it to Reader
        String dataTypeDirectoryPath;
        if(expDataType.getDataTypeType().equals("validation")) {
            dataTypeDirectoryPath = ".\\resources\\files\\dataType\\validation\\" + user.getId();
            propertiesDto.setValidationPath(dataTypeDirectoryPath);
            propertiesDto.setTrainingPath(dataTypeDirectoryPath +  "\\" + expDto.getExperimentName().replaceAll("\\s+", "") + expDataTypeDto.getDataTypeName().replaceAll("\\s+", "") + ".csv");
            propertiesDto.setValidation(true);
        }
        else if(expDataType.getDataTypeType().equals("test")){
            dataTypeDirectoryPath = ".\\resources\\files\\dataType\\test\\" + user.getId();
            propertiesDto.setTestPath(dataTypeDirectoryPath);
            propertiesDto.setTrainingPath(dataTypeDirectoryPath + "\\" + expDto.getExperimentName().replaceAll("\\s+", "") + expDataTypeDto.getDataTypeName().replaceAll("\\s+", "") + ".csv");   // TEMPORAL UNTIL KNOW IF WE NEED THIS OR NOT
            propertiesDto.setTest(true);
        }
        else {      // Training
            dataTypeDirectoryPath = ".\\resources\\files\\dataType\\training\\" + user.getId();
            propertiesDto.setTrainingPath(dataTypeDirectoryPath +  "\\" + expDto.getExperimentName().replaceAll("\\s+", "") + expDataTypeDto.getDataTypeName().replaceAll("\\s+", "") + ".csv");   // TEMPORAL UNTIL KNOW IF WE NEED THIS OR NOT
            propertiesDto.setTraining(true);
        }

        new File(".\\resources\\files\\properties\\" + user.getId()).mkdirs(); // Create the directory to save datatype files
        String propertiesFilePath = ".\\resources\\files\\properties\\" + user.getId() + "\\" + expDto.getExperimentName().replaceAll("\\s+", "") + ".properties";

        createPropertiesFile(propertiesFilePath, propertiesDto, expDto.getExperimentName(), currentTimestamp);  // Write in property file

        // END - Create PropertiesDto file

        if(radioDataType.equals("on")) {    // NULL if didn't select the dataType file from the list - ON if th:value is empty
            File tmpFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") +
                    multipartFile.getOriginalFilename());
            multipartFile.transferTo(tmpFile);

            Reader reader = new FileReader(tmpFile);
            experimentService.loadExperimentRowTypeFile(reader, expDataType);   // Save row here
            reader.close();

            new File(dataTypeDirectoryPath).mkdirs(); // Create the directory to save datatype files

            InputStream dataTypeInputStream = null;
            OutputStream dataTypeOutputStream = null;

            dataTypeInputStream = multipartFile.getInputStream();

            String dataTypeFilePath = dataTypeDirectoryPath + "\\" + expDto.getExperimentName().replaceAll("\\s+", "") + expDataTypeDto.getDataTypeName().replaceAll("\\s+", "") + ".csv";;

            File dataTypeNewFile = new File(dataTypeFilePath);
            if (!dataTypeNewFile.exists()) {
                dataTypeNewFile.createNewFile();
            }
            dataTypeOutputStream = new FileOutputStream(dataTypeNewFile);
            int dataTypeRead = 0;
            byte[] dataTypeBytes = new byte[1024];

            while ((dataTypeRead = dataTypeInputStream.read(dataTypeBytes)) != -1) {
                dataTypeOutputStream.write(dataTypeBytes, 0, dataTypeRead);
            }
        }
        else{   // DataType selected from list
            // We won't save it, because already exists. But we are going to use it in our execution
            Long idDataType = Long.parseLong(radioDataType);

            ExperimentDataType expDataTypeSelected = experimentService.findExperimentDataTypeById(idDataType);

            // AQUÍ YA TENEMOS EL ARCHIVO LOCALIZADOS - HACE FALTA CARGARLO CUANDO SE EJECUTE EL PROGRAMA
        }
        // END Reader - FILE DATA TYPE

        experimentService.saveGrammar(grammar);

        runService.saveRun(run);
        // END CONFIGURATION SECTION

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

        /*String examplePath = ".\\resources\\example\\example.properties";
        File exampleFile = new File(examplePath);
        if (!exampleFile.exists()) {
            exampleFile.createNewFile();
        }

        OutputStream out = new FileOutputStream(exampleFile);

        properties.store(out, "example");*/

        int numObjectives = 1;  // Example
        int threadId = 1;

        executeGramEv(properties, threadId, numObjectives);        // PropertiesDto properties, int threadId, int numObjectives

        propertiesReader.close();
        // END - Execute program with experiment info

        return "/user/experiment/configExperiment";
    }

    @RequestMapping(value="/user/experiment/experimentRepository", method=RequestMethod.GET)
    public String experimentRepository(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {    // User not authenticated
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        User user = userService.findByEmail(authentication.getName());

        List<Experiment> lExperiment = experimentService.findByUserId(user);

        model.addAttribute("experimentList", lExperiment);
        model.addAttribute("user", user);

        return "/user/experiment/experimentRepository";
    }

    @RequestMapping(value="/user/experiment/expRepoSelected")
    public String expRepoSelected(Model model,
                                  @RequestParam String id){ // Exp ID

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {    // User not authenticated
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        User user = userService.findByEmail(authentication.getName());

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
                          @RequestParam String id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {    // User not authenticated
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        User user = userService.findByEmail(authentication.getName());

        Long idRun = Long.parseLong(id);
        Run run = runService.findByUserIdAndId(user, idRun);
        Experiment expConfig = run.getExperimentId();
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

    /** Run gramEv application
     *
     */

    public void executeGramEv(Properties properties, int threadId, int numObjectives) throws IOException {
        GramEvalTemporalModel gramEvalTemporalModel = new GramEvalTemporalModel(properties, threadId, numObjectives);
        gramEvalTemporalModel.runGE(properties, threadId, null);
    }


    public void createPropertiesFile(String propertiesFilePath, PropertiesDto propertiesDto, String expName, java.sql.Date currentTimeStamp) throws IOException {
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

    // for execute a jar file = java -jar Main.jar

}