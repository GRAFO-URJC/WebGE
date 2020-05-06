package com.engine.algorithm;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.DiagramPair;
import com.gramevapp.web.model.Run;

import java.util.Properties;

import static com.engine.util.Common.OBJECTIVES_PROP;

public class RunnableExpGramEv implements Runnable {

    private final Properties properties;
    private final DiagramData diagramData;
    private final Run runElement;
    private SymbolicRegressionGE ge;

    public RunnableExpGramEv(Properties properties, DiagramData diagramData, Run runElement) {
        this.properties = properties;
        this.diagramData = diagramData;
        this.runElement = runElement;
    }

    @Override
    public void run() {

        int numObjectives = 1;
        if ((properties.getProperty(OBJECTIVES_PROP) != null)
                && (Integer.valueOf(properties.getProperty(OBJECTIVES_PROP)) == 2)) {
            numObjectives = 2;
        }

        runElement.setStatus(Run.Status.INITIALIZING);
        runElement.setBestIndividual(diagramData.getBestIndividual());
        runElement.setCurrentGeneration(diagramData.getCurrentGeneration());

        ge = new SymbolicRegressionGE(properties,numObjectives);

        RunGeObserver observer = new RunGeObserver();
        diagramData.setFinished(false);
        diagramData.setStopped(false);
        diagramData.setFailed(false);
        observer.setDiagramData(diagramData);

        ge.runGE(observer);
    }

    public void stopExecution() {
        ge.stopExecution();
    }

    public String getModel(){
        return this.ge.getModel();
    }
}
