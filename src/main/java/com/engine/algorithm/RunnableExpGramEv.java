package com.engine.algorithm;

import com.gramevapp.web.model.Dataset;
import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.RunService;

import java.util.Properties;

import static com.engine.util.Common.OBJECTIVES_PROP;

public class RunnableExpGramEv implements Runnable {

    private final Properties properties;
    private final Run runElement;
    private SymbolicRegressionGE ge;
    private Dataset experimentDataType;
    private RunService runService;
    private int crossRunIdentifier;

    public RunnableExpGramEv(Properties properties, Run runElement, Dataset experimentDataType,
                             RunService runService, int crossRunIdentifier) {
        this.properties = properties;
        this.runElement = runElement;
        this.experimentDataType = experimentDataType;
        this.runService = runService;
        this.crossRunIdentifier = crossRunIdentifier;
    }

    @Override
    public void run() {

        int numObjectives = 1;
        if ((properties.getProperty(OBJECTIVES_PROP) != null)
                && (Integer.parseInt(properties.getProperty(OBJECTIVES_PROP)) == 2)) {
            numObjectives = 2;
        }

        runElement.setStatus(Run.Status.INITIALIZING);
        runElement.setBestIndividual(0.0);
        runElement.setCurrentGeneration(0);

        ge = new SymbolicRegressionGE(properties, numObjectives);

        RunGeObserver observer = new RunGeObserver();
        observer.setDiagramData(runElement);
        String datasetInfo = experimentDataType.getInfo();
        if (datasetInfo.contains("K-Fold")) {
            String[] splitInfo = datasetInfo.split("\r\n");
            datasetInfo = "";
            datasetInfo += splitInfo[0].substring(0, splitInfo[0].length() - ";K-Fold".length()) + "\r\n";
            //index last ;
            int indexFold, identifier;
            for (int i = 1; i < splitInfo.length; i++) {
                indexFold = splitInfo[i].lastIndexOf(';');
                identifier = Integer.parseInt(splitInfo[i].substring(indexFold + 1));
                //check if have cross
                if (crossRunIdentifier < 0 || identifier != crossRunIdentifier) {
                    datasetInfo += splitInfo[i].substring(0, indexFold) + "\r\n";
                }
            }
        }

        ge.runGE(observer, datasetInfo, runElement, runService);
    }

    public void stopExecution() {
        ge.stopExecution();
    }

}
