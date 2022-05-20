package com.engine.algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gramevapp.web.LoggingAccessDeniedHandler;
import com.gramevapp.web.model.Dataset;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.SaveDBService;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static com.engine.util.Common.OBJECTIVES_PROP;

public class RunnableExpGramEv implements Runnable {

    //private static AtomicLong idGenerator = new AtomicLong(0);

    private Properties properties;
    private Run runElement;
    private SymbolicRegressionGE ge;
    private Dataset experimentDataType;
    private RunService runService;
    private int crossRunIdentifier;
    private SaveDBService saveDBService;
    private String objective;
    private boolean de;
    //private Long runnablesKey;

    public RunnableExpGramEv(
            @JsonProperty("properties") Properties properties,
            @JsonProperty("runElement") Run runElement,
            @JsonProperty("experimentDataType") Dataset experimentDataType,
            @JsonProperty("runService") RunService runService,
            @JsonProperty("saveDBService") SaveDBService saveDBService,
            @JsonProperty("crossRunIdentifier") int crossRunIdentifier,
            @JsonProperty("objective") String objective,
            @JsonProperty("de") boolean de) {

        this.properties = properties;
        this.runElement = runElement;
        this.experimentDataType = experimentDataType;
        this.runService = runService;
        this.crossRunIdentifier = crossRunIdentifier;
        this.saveDBService = saveDBService;
        this.objective = objective;
        this.de = de;
    }

    @Override
    public void run() {
        Logger logger = Logger.getLogger(RunnableExpGramEv.class.getName());
        int numObjectives = 1;
        if(properties == null) {
            logger.warning("PROPERTIES ES NULL");
        }
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
    }

    public void stopExecution() {
        ge.stopExecution();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Run getRunElement() {
        return runElement;
    }

    public void setRunElement(Run runElement) {
        this.runElement = runElement;
    }

    public SymbolicRegressionGE getGe() {
        return ge;
    }

    public void setGe(SymbolicRegressionGE ge) {
        this.ge = ge;
    }

    public Dataset getExperimentDataType() {
        return experimentDataType;
    }

    public void setExperimentDataType(Dataset experimentDataType) {
        this.experimentDataType = experimentDataType;
    }

    public RunService getRunService() {
        return runService;
    }

    public void setRunService(RunService runService) {
        this.runService = runService;
    }

    public int getCrossRunIdentifier() {
        return crossRunIdentifier;
    }

    public void setCrossRunIdentifier(int crossRunIdentifier) {
        this.crossRunIdentifier = crossRunIdentifier;
    }

    public SaveDBService getSaveDBService() {
        return saveDBService;
    }

    public void setSaveDBService(SaveDBService saveDBService) {
        this.saveDBService = saveDBService;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public boolean isDe() {
        return de;
    }

    public void setDe(boolean de) {
        this.de = de;
    }
}
