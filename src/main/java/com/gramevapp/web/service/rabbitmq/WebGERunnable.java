package com.gramevapp.web.service.rabbitmq;

import com.engine.algorithm.*;
import com.gramevapp.web.model.Dataset;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.SaveDBService;
import jeco.core.algorithm.sge.AbstractGECommon;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.logging.Logger;

import static com.engine.util.Common.OBJECTIVES_PROP;

public class WebGERunnable implements Runnable {
    private Properties properties;
    private Run runElement;
    private AbstractGECommon ge;

    private CommonBehaviour common;
    private Dataset experimentDataType;
    private RunService runService;
    private int crossRunIdentifier;
    private SaveDBService saveDBService;
    private String objective;
    private boolean de;
    private RabbitTemplate rabbitTemplate;

    public WebGERunnable(WebGERunnableUtils utils, RunService runService, SaveDBService saveDBService, RabbitTemplate rabbitTemplate) {
        this.properties = utils.getProperties();
        this.experimentDataType = utils.getExperimentDataType();
        this.runService = runService;
        this.runElement = runService.findByRunId(utils.getRunId());
        this.crossRunIdentifier = utils.getCrossRunIdentifier();
        this.saveDBService = saveDBService;
        this.objective = utils.getObjective();
        this.de = utils.getDe();
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run() {
        Logger logger = Logger.getLogger(WebGERunnable.class.getName());
        int numObjectives = 1;
        if(properties == null) {
            logger.warning("PROPERTIES ES NULL");
        }
        else if ((properties.getProperty(OBJECTIVES_PROP) != null)
                && (Integer.parseInt(properties.getProperty(OBJECTIVES_PROP)) == 2)) {
            numObjectives = 2;
        }

        runElement.setStatus(Run.Status.INITIALIZING);
        runElement.setBestIndividual(0.0);
        runElement.setCurrentGeneration(0);


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
        if(properties.getProperty("GrammaticalEvolutionType").equals("GE")) {
            SymbolicRegressionGE symR = new SymbolicRegressionGE(properties, numObjectives, objective, de, rabbitTemplate);
            symR.runGE(observer, datasetInfo.toString(), runElement, runService, saveDBService);
            common = symR.getCommon();
            ge = symR;
        }else if(properties.getProperty("GrammaticalEvolutionType").equals("SGE")){
            StaticSGE SGE = new StaticSGE(properties, numObjectives, objective, de, rabbitTemplate);
            SGE.runGE(observer, datasetInfo.toString(), runElement, runService, saveDBService);
            common = SGE.getCommon();
            ge = SGE;
        }else if(properties.getProperty("GrammaticalEvolutionType").equals("DSGE")){
            DynamicSGE DSGE = new DynamicSGE(properties, numObjectives, objective, de, rabbitTemplate);
            DSGE.runGE(observer, datasetInfo.toString(), runElement, runService, saveDBService);
            common = DSGE.getCommon();
            ge = DSGE;
        }


    }

    public void stopExecution() {
        common.stopExecution();
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

    public AbstractGECommon getGe() {
        return ge;
    }

    public void setGe(AbstractGECommon ge) {
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





