package com.gramevapp.web.service;

import com.gramevapp.web.model.*;
import com.gramevapp.web.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public Grammar saveGrammar(Grammar grammar) {
        return grammarRepository.save(grammar);
    }

    public Experiment saveExperiment(Experiment experiment) {
        return experimentRepository.save(experiment);
    }

    public void updateExperiment() {
        experimentRepository.flush();
    }

    public void updataExpDataType() {
        experimentDataTypeRepository.flush();
    }

    public void updateGrammar() {
        grammarRepository.flush();
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

    public ExpProperties findPropertiesByExpIdAndRunId(Long expId, Long runId) {
        return propertiesRepository.findByIdExpAndIdRun(expId, runId);
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

    public void deleteDataTypeFile(ExperimentDataType experimentDataType) {
        experimentDataTypeRepository.delete(experimentDataType);
    }

    public void deleteDataTypeFile(Long id) {
        experimentDataTypeRepository.delete(this.findDataTypeById(id));
    }

    public void deleteGrammar(Grammar grammar) {
        grammarRepository.delete(grammar);
    }

    public List<Experiment> findAll() {
        return experimentRepository.findAll();
    }
}