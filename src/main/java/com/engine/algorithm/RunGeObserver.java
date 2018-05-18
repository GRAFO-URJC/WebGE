package com.engine.algorithm;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.other.BeanUtil;
import com.gramevapp.web.service.DiagramDataService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * Stores into the database the values generated by RunGE execution.
 *
 */

@Service("RunGe")
public class RunGeObserver implements Observer {

    private DiagramData diagramData;

    @Override
    public void update(Observable o, Object arg) {
        HashMap<String, Object> dataMap = (HashMap<String, Object>) arg;

        // Taking data from dictionary
        // Actual generation
        int currGen = Integer.valueOf(dataMap.get("CurrentGeneration").toString());
        // Porcentaje de ejecución Execution percentage
        int currPercent = Math.round((currGen * 100) / Integer.valueOf(dataMap.get("MaxGenerations").toString()));

        // One of these two are null

        // Current value of best individual
        double currBest = Double.valueOf(dataMap.get("BestObjective").toString());

        // Multi-objetive shows all objectives (Not necessary but do not erase)
        double objs[][] = (double [][]) dataMap.get("Objectives");

        this.diagramData.setBestIndividual(currBest);

        // http://codippa.com/how-to-autowire-objects-in-non-spring-classes/
        //get application context
        ApplicationContext context = BeanUtil.getAppContext();
        // get instance of MainSpringClass (Spring Managed class)
        DiagramDataService dataDataService = (DiagramDataService)context.getBean("diagramDataService");
        // use this spring object to call its methods

        dataDataService.saveDiagram(this.diagramData);

        /*
        if (dataMap.get("BestObjective") != null) {
            currBest = Double.valueOf(dataMap.get("BestObjective").toString());

        } else if (dataMap.get("Objectives") != null) {
            double objs[][] = (double [][]) dataMap.get("Objectives");
        }
         */
    }

    public DiagramData getDiagramData() {
        return diagramData;
    }

    public void setDiagramData(DiagramData diagramData) {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        this.diagramData = diagramData;
    }
}
