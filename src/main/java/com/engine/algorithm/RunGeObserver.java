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

    private Run run;
    private Lock lock = new ReentrantLock();

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

        if (this.run.getStatus().equals(Run.Status.INITIALIZING)) {
            this.run = runService.findByRunId(run.getId());
            run.setStatus(Run.Status.RUNNING);
            dataDataService.saveRun(run);
        }

        lock.lock();
        this.run = runService.findByRunId(run.getId());
        DiagramData diagramData = new DiagramData();

        if (currPercent == 100 || currBest <= 0.0)
            diagramData.setFinished(true);

        diagramData.setBestIndividual(Math.max(currBest, 0.0));
        diagramData.setCurrentGeneration(currGen);
        diagramData.setRunId(run);
        dataDataService.saveDiagram(diagramData);

        this.run = runService.findByRunId(run.getId());
        run.setModificationDate(new Timestamp(new Date().getTime()));
        runService.saveRun(run);
        lock.unlock();
    }

    public void setDiagramData(Run run) {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        this.run = run;
    }

    public Lock getLock() {
        return lock;
    }
}
