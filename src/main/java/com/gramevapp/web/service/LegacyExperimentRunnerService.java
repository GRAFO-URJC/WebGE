package com.gramevapp.web.service;

import com.engine.algorithm.RunnableExpGramEv;
import com.gramevapp.web.model.Run;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import java.util.logging.Logger;

import static com.engine.util.Common.TRAINING_PATH_PROP;

@Service
public class LegacyExperimentRunnerService implements ExperimentRunner{

    private List<Thread> threads;
    private ExperimentService experimentService;
    private SaveDBService saveDBService;
    private Map<Long, Thread> threadMap;
    private Logger logger;
    private RunService runService;
    private Map<String, Long> threadRunMap;
    private Map<Long, RunnableExpGramEv> runnables;



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

    @Override
    public void accept(Run run) {
        //threads.add(runExperimentDetails(run, propPath, crossRunIdentifier, configExpDto.getObjective(), configExpDto.isDe()));
    }

    // Metodos del controller, con mas argumentos

    private Thread runExperimentDetails(Run run, String propPath, int crossRunIdentifier, String objective, boolean de) throws IOException {

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
}
