package com.engine.algorithm;

import com.gramevapp.web.model.Dataset;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.SaveDBService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Properties;
import java.util.concurrent.Callable;

import static com.engine.util.Common.OBJECTIVES_PROP;

// Clon de RunnableExpGramEv pero implementando Callable
public class CallableExpGramEv implements Callable<Void> {

    private Properties properties;
    private Run runElement;
    private SymbolicRegressionGE ge;
    private Dataset experimentDataType;
    private RunService runService;
    private int crossRunIdentifier;
    private SaveDBService saveDBService;
    private String objective;
    private boolean de;

    private RabbitTemplate rabbitTemplate;

    public CallableExpGramEv(Properties properties, Run runElement, Dataset experimentDataType, RunService runService
            , SaveDBService saveDBService, int crossRunIdentifier, String objective, boolean de, RabbitTemplate rabbitTemplate) {
        this.properties = properties;
        this.runElement = runElement;
        this.experimentDataType = experimentDataType;
        this.runService = runService;
        this.crossRunIdentifier = crossRunIdentifier;
        this.saveDBService = saveDBService;
        this.objective = objective;
        this.de = de;
        this.rabbitTemplate = rabbitTemplate;
    }

    public CallableExpGramEv() {

    }

    @Override
    public Void call() throws Exception {
        int numObjectives = 1;
        if ((properties.getProperty(OBJECTIVES_PROP) != null)
                && (Integer.parseInt(properties.getProperty(OBJECTIVES_PROP)) == 2)) {
            numObjectives = 2;
        }

        //runElement.setStatus(Run.Status.INITIALIZING);
        runElement.setBestIndividual(0.0);
        runElement.setCurrentGeneration(0);


        ge = new SymbolicRegressionGE(properties, numObjectives, objective, de, rabbitTemplate);


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
