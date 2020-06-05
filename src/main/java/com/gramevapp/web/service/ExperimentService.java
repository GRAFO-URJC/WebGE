package com.gramevapp.web.service;

import com.gramevapp.web.model.*;
import com.gramevapp.web.repository.ExperimentDataTypeRepository;
import com.gramevapp.web.repository.ExperimentRepository;
import com.gramevapp.web.repository.GrammarRepository;
import com.gramevapp.web.repository.PropertiesRepository;
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

    @Autowired
    private PropertiesRepository propertiesRepository;

    public void saveDataType(ExperimentDataType expDataType) {
        experimentDataTypeRepository.save(expDataType);
    }

    public void saveExperiment(Experiment experiment) {
        experimentRepository.save(experiment);
    }

    public List<Experiment> findByUser(User user) {
        return experimentRepository.findByUserId(user);
    }

    public Experiment findExperimentByUserIdAndExpId(User user, Long expId) {
        return experimentRepository.findByUserIdAndId(user, expId);
    }

    public Grammar findGrammarById(Long grammarId) {
        return grammarRepository.findGrammarById(grammarId);
    }

    public ExperimentDataType findDataTypeById(Long dataTypeId) {
        Optional<ExperimentDataType> check = experimentDataTypeRepository.findById(dataTypeId);
        return check.orElse(null);
    }

    public Experiment findExperimentById(Long id) {
        Optional<Experiment> check = experimentRepository.findById(id);
        return check.orElse(null);
    }

    public ExperimentDataType findExperimentDataTypeById(Long id) {
        Optional<ExperimentDataType> check = experimentDataTypeRepository.findById(id);
        return check.orElse(null);
    }

    public ExpProperties findPropertiesById(Long propertiesId) {
        Optional<ExpProperties> check = propertiesRepository.findById(propertiesId);
        return check.orElse(null);
    }

    public void deleteExpProperties(ExpProperties expProperties) {
        propertiesRepository.delete(expProperties);
    }

    public ExpProperties saveExpProperties(ExpProperties expProperties) {
        return propertiesRepository.save(expProperties);
    }

    public List<ExperimentDataType> findAllExperimentDataTypeByUserId(Long userId) {
        return experimentDataTypeRepository.findAllByUserIdUserId(userId);
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