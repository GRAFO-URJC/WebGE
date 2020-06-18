package com.engine.algorithm;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.other.BeanUtil;
import com.gramevapp.web.service.DiagramDataService;
import com.gramevapp.web.service.RunService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Stores into the database the values generated by RunGE execution.
 */

@Service("RunGe")
public class RunGeObserver implements Observer {

    private DiagramData diagramData;
    private Lock lock= new ReentrantLock();

    @Override
    public void update(Observable o, Object arg) {
        HashMap<String, Object> dataMap = (HashMap<String, Object>) arg;

        // Taking data from dictionary
        // Actual generation
        int currGen = Integer.parseInt(dataMap.get("CurrentGeneration").toString());
        // Porcentaje de ejecución Execution percentage
        int currPercent = Math.round((currGen * 100) / Integer.parseInt(dataMap.get("MaxGenerations").toString()));

        // One of these two are null

        // Current value of best individual
        double currBest = Double.parseDouble(dataMap.get("BestObjective").toString());

        // http://codippa.com/how-to-autowire-objects-in-non-spring-classes/
        //get application context
        ApplicationContext context = BeanUtil.getAppContext();
        DiagramDataService dataDataService = (DiagramDataService) context.getBean("diagramDataService");
        RunService runService = (RunService) context.getBean("runService");


        if (this.diagramData.getRunId().getStatus().equals(Run.Status.INITIALIZING)) {
            Run run = runService.findByRunId(this.diagramData.getRunId().getId());
            run.setStatus(Run.Status.RUNNING);
            dataDataService.saveRun(run);
        }

        if (currPercent == 100 || currBest <= 0.0)
            this.diagramData.setFinished(true);

        this.diagramData.setBestIndividual(Math.max(currBest, 0.0));
        this.diagramData.setCurrentGeneration(currGen);
        this.diagramData.setId(null);
        this.diagramData=dataDataService.saveDiagram(this.diagramData);

        lock.lock();
        Run updateRun= runService.findByRunId(this.diagramData.getRunId().getId());
        updateRun.setModificationDate(new Timestamp(new Date().getTime()));
        runService.saveRun(updateRun);
        lock.unlock();
    }

    public DiagramData getDiagramData() {
        return diagramData;
    }

    public void setDiagramData(DiagramData diagramData) {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        this.diagramData = diagramData;
    }

    public Lock getLock(){
        return lock;
    }
}
