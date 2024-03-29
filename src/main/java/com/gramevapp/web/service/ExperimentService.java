package com.gramevapp.web.service;

import com.gramevapp.web.model.Dataset;
import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.User;
import com.gramevapp.web.repository.ExperimentDataTypeRepository;
import com.gramevapp.web.repository.ExperimentRepository;
import com.gramevapp.web.repository.GrammarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// We use repositories ExperimentDataType, ExperimentRowType here too.
@Service("experimentService")
public class ExperimentService {

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private GrammarRepository grammarRepository;

    @Autowired
    private ExperimentDataTypeRepository experimentDataTypeRepository;

    public void saveDataType(Dataset expDataType) {
        experimentDataTypeRepository.save(expDataType);
    }

    public void saveExperiment(Experiment experiment) {
        experimentRepository.save(experiment);
    }

    public List<Experiment> findByUser(User user) {
        return experimentRepository.findByUserIdOrderByCreationDateDesc(user.getId());
    }

    /**
     * Returns experiments without any of the related objects.
     * @param user
     * @return
     */
    public List<Experiment> findByUserOptimized(User user) {
        return experimentRepository.findByUserIdOrderByCreationDateDescOptimized(user.getId());
    }

    public Experiment findLastExperimentByUserOptimized(User user) {
        return experimentRepository.findByUserIdMostRecentExperimentOptimized(user.getId());
    }

    public Dataset findDataTypeById(Long dataTypeId) {
        Optional<Dataset> check = experimentDataTypeRepository.findById(dataTypeId);
        return check.orElse(null);
    }

    public Experiment findExperimentById(Long id) {
        Optional<Experiment> check = experimentRepository.findById(id);
        return check.orElse(null);
    }

    public Dataset findExperimentDataTypeById(Long id) {
        Optional<Dataset> check = experimentDataTypeRepository.findById(id);
        return check.orElse(null);
    }

    public List<Dataset> findAllExperimentDataTypeByUserId(Long userId) {
        return experimentDataTypeRepository.findAllByUserIdUserIdOrderByCreationDateDesc(userId);
    }

    public Dataset findExperimentDataTypeByDataTypeNameAndUserId( String name, Long userId){
        return experimentDataTypeRepository.findByDataTypeNameAndUserIdUserId (name, userId);
    }

    public void deleteExperiment(Experiment experiment) {
        experimentRepository.delete(experiment);
    }

    public void deleteDataTypeFile(Long id) {
        experimentDataTypeRepository.delete(this.findDataTypeById(id));
    }

    public List<Experiment> findAll() {
        return experimentRepository.findAll();
    }

}