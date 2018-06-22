package com.gramevapp.web.service;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.DiagramPair;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.repository.DiagramDataRepository;
import com.gramevapp.web.repository.DiagramPairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service("diagramDataService")
@Transactional
public class DiagramDataService {

    @Autowired
    private DiagramDataRepository diagramRepository;

    @Autowired
    private DiagramPairRepository diagramPairRepository;

    public DiagramData getLastBestIndividual(Run runId){
        return diagramRepository.findByRunId(runId);
    }

    public DiagramData findByRunId(Run runId){
        return diagramRepository.findByRunId(runId);
    }

    public DiagramData saveDiagram(DiagramData diagramData) {
        return diagramRepository.save(diagramData);
    }

    public void saveDiagramPairList(List<DiagramPair> diagramPairList) {
        for(DiagramPair dp : diagramPairList)
        diagramPairRepository.save(dp);
    }

    public void saveDiagramPair(DiagramPair diagramPair) {
        diagramPairRepository.save(diagramPair);
    }


    /*public void deleteDiagramPairList(List<DiagramPair> diagramPairList){
        for(DiagramPair dp : diagramPairList)
            diagramPairRepository.deleteAllBy(dp.getId());
    }*/

    public void deleteAllByDiagramId(DiagramData diagramDataId){
        diagramPairRepository.deleteAllByDiagramDataId(diagramDataId);
    }

    /*public boolean emptyList(List<DiagramPair> lDiagramPair){
        Long id = lDiagramPair.get(0).getId();

        List<DiagramPair> lDP = diagramPairRepository.findByDiagramDataId(id);
        return lDP.isEmpty();
    }*/

}
