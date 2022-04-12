package com.engine.algorithm;

import com.gramevapp.web.model.Dataset;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.SaveDBService;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import static com.engine.util.Common.OBJECTIVES_PROP;

// Clon de RunnableExpGramEv pero implementando Callable
public class CallableExpGramEv implements Callable<Void> {

    private static AtomicLong idGenerator = new AtomicLong(0);

    private final Properties properties;
    private final Run runElement;
    private SymbolicRegressionGE ge;
    private Dataset experimentDataType;
    private RunService runService;
    private int crossRunIdentifier;
    private SaveDBService saveDBService;
    private String objective;
    private boolean de;
    private Long callablesKey;

    public CallableExpGramEv(Properties properties, Run runElement, Dataset experimentDataType,
                             RunService runService, SaveDBService saveDBService, int crossRunIdentifier, String objective, boolean de) {
        this.properties = properties;
        this.runElement = runElement;
        this.experimentDataType = experimentDataType;
        this.runService = runService;
        this.crossRunIdentifier = crossRunIdentifier;
        this.saveDBService = saveDBService;
        this.objective = objective;
        this.de = de;
        this.callablesKey = idGenerator.incrementAndGet();
    }

    public long getCallablesKey() { return this.callablesKey; }

    @Override
    public Void call() throws Exception {
        int numObjectives = 1;
        if ((properties.getProperty(OBJECTIVES_PROP) != null)
                && (Integer.parseInt(properties.getProperty(OBJECTIVES_PROP)) == 2)) {
            numObjectives = 2;
        }

        runElement.setStatus(Run.Status.INITIALIZING);
        runElement.setBestIndividual(0.0);
        runElement.setCurrentGeneration(0);

        ge = new SymbolicRegressionGE(properties, numObjectives, objective, de);

        RunGeObserver observer = new RunGeObserver();
        observer.setDiagramData(runElement);
        StringBuilder datasetInfo = new StringBuilder(experimentDataType.getInfo());
        if (datasetInfo.toString().contains("K-Fold")) {
            String[] splitInfo = datasetInfo.toString().split("\r\n");
            datasetInfo = new StringBuilder();
            datasetInfo.append(splitInfo[0].substring(0, splitInfo[0].length() - ";K-Fold".length())).append("\r\n");

            int indexFold;
            int identifier;
            for (int i = 1; i < splitInfo.length; i++) {
                indexFold = splitInfo[i].lastIndexOf(';');
                identifier = Integer.parseInt(splitInfo[i].substring(indexFold + 1));
                //check if have cross
                if (crossRunIdentifier < 0 || identifier != crossRunIdentifier) {
                    String stringBuilder = splitInfo[i].substring(0, indexFold) +
                            "\r\n";
                    datasetInfo.append(stringBuilder);
                }
            }
        }

        ge.runGE(observer, datasetInfo.toString(), runElement, runService, saveDBService);
        return null;
    }

    public void stopExecution() {
        ge.stopExecution();
    }
}
