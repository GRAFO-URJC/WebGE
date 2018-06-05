package com.gramevapp.web.service;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.repository.DiagramDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("diagramDataService")
public class DiagramDataService {

    @Autowired
    private DiagramDataRepository diagramRepository;

    public void saveDiagram(DiagramData diagramData) {
        diagramRepository.save(diagramData);
    }

    public DiagramData getLastBestIndividual(Run runId){
        // Long longRunId = runId.getId();
        return diagramRepository.findByRunId(runId);
    }

    public DiagramData findByRunId(Run runId){
        return diagramRepository.findByRunId(runId);
    }
}
