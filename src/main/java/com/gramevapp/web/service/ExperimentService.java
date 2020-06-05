package com.gramevapp.web.service;

import com.gramevapp.web.model.*;
import com.gramevapp.web.repository.ExperimentDataTypeRepository;
import com.gramevapp.web.repository.ExperimentRepository;
import com.gramevapp.web.repository.GrammarRepository;
import com.gramevapp.web.repository.PropertiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public ExperimentDataType saveDataType(ExperimentDataType expDataType) {
        return experimentDataTypeRepository.save(expDataType);
    }

    public Experiment saveExperiment(Experiment experiment) {
        return experimentRepository.save(experiment);
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
        return experimentDataTypeRepository.findById(dataTypeId).get();
    }

    public Experiment findExperimentById(Long id) {
        return experimentRepository.findById(id).get();
    }

    public ExperimentDataType findExperimentDataTypeById(Long id) {
        return experimentDataTypeRepository.findById(id).get();
    }

    public ExpProperties findPropertiesById(Long propertiesId) {
        return propertiesRepository.findById(propertiesId).get();
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