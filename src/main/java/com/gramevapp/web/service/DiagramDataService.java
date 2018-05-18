package com.gramevapp.web.service;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.other.BeanUtil;
import com.gramevapp.web.repository.DiagramDataRepository;
import com.gramevapp.web.repository.RunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("diagramDataService")
public class DiagramDataService {

    @Autowired
    private DiagramDataRepository diagramRepository;

    @Autowired
    private RunRepository runRepository;

    public void saveDiagram(DiagramData diagramData) {
        diagramRepository.save(diagramData);
    }

    public DiagramData getLastBestIndividual(Run runId){

        // http://codippa.com/how-to-autowire-objects-in-non-spring-classes/
        //get application context
        /*ApplicationContext context = BeanUtil.getAppContext();
        // get instance of MainSpringClass (Spring Managed class)
        RunService runService = (RunService) context.getBean("runService");*/
        // use this spring object to call its methods

        //Run run = runRepository.findByRunName("a");
        //Run run = runRepository.findByRunId(runId);
        return diagramRepository.findByRunId(runId);
    }
}
