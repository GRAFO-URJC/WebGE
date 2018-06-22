package com.gramevapp.web.service;

import com.gramevapp.web.model.*;
import com.gramevapp.web.repository.*;
import com.opencsv.bean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

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
    private ExperimentRowTypeRepository experimentRowTypeRepository;

    @Autowired
    private PropertiesRepository propertiesRepository;

    // Add ExperimentDataType file into the DD.BB. - Just the Validation, Test, Training text
    // - This means read line by line the file, create a ExperimentRowType by line,
    // add the line in the list and upload the row in the DDBB
    public void loadExperimentRowTypeFile(Reader fileTypeReader, ExperimentDataType expDataType){
        Scanner scanCsv = new Scanner(new BufferedReader(fileTypeReader));

        ArrayList<String> columnList = new ArrayList<>();

        String line = scanCsv.nextLine();
        String[] columns = line.split(";");
        for(String column : columns) {
            columnList.add(column);
        }

        expDataType.setHeader(columnList);

        while(scanCsv.hasNext()){
            String row = scanCsv.next();

            ArrayList<String> rowColumnList = new ArrayList<>();

            String[] rowColumns = row.split(";");
            for(String rowColumn : rowColumns) {
                rowColumnList.add(rowColumn);
            }

            ExperimentRowType expRowType = new ExperimentRowType();
            expRowType.setExpDataTypeId(expDataType);
            expRowType.setDataRow(rowColumnList);

            expDataType.addExperimentRowType(expRowType);

            experimentRowTypeRepository.save(expRowType);
        }
        scanCsv.close();

    }

    public ExperimentDataType saveDataType(ExperimentDataType expDataType){
        return experimentDataTypeRepository.save(expDataType);
    }

    public Grammar saveGrammar(Grammar grammar){
        return grammarRepository.save(grammar);
    }

    public Experiment saveExperiment(Experiment experiment){
        return experimentRepository.save(experiment);
    }

    public void updateExperiment(){
        experimentRepository.flush();
    }

    public void updataExpDataType(){
        experimentDataTypeRepository.flush();
    }

    public List<Experiment> findByUserId(User user) {
        return experimentRepository.findByUserId(user);
    }

    public Experiment findExperimentByUserIdAndExpId(User user, Long expId){
        return experimentRepository.findByUserIdAndId(user, expId);
    }

    public Grammar findGrammarById(Long grammarId){
        return grammarRepository.findGrammarById(grammarId);
    }

    public ExperimentDataType findDataTypeById(Long dataTypeId){
        return experimentDataTypeRepository.findById(dataTypeId);
    }

    public Experiment findExperimentById(Long id)
    {
        return experimentRepository.findById(id);
    }

    public ExperimentDataType findExperimentDataTypeById(Long id){
        return experimentDataTypeRepository.findById(id);
    }

    public ExpProperties findPropertiesById(Long propertiesId){
        return propertiesRepository.findById(propertiesId);
    }

    public ExpProperties findPropertiesByExpIdAndRunId(Long expId, Long runId){
        return propertiesRepository.findByIdExpAndIdRun(expId, runId);
    }

    public void deleteExpProperties(ExpProperties expProperties){
        propertiesRepository.delete(expProperties);
    }

    public ExpProperties saveExpProperties(ExpProperties expProperties){
        return propertiesRepository.save(expProperties);
    }

    public List<Grammar> findAllGrammarByExperimentId(Experiment experimentId){
        return grammarRepository.findAllByExperimentId(experimentId);
    }

    public List<ExperimentDataType> findAllExperimentDataTypeByExperimentId(Experiment experimentId){
        return experimentDataTypeRepository.findAllByExperimentId(experimentId);
    }

    public void deleteExperiment(Experiment experiment){
        experimentRepository.delete(experiment);
    }

    public void deleteDataTypeFile(ExperimentDataType experimentDataType){
        experimentDataTypeRepository.delete(experimentDataType);
    }

    public void deleteGrammar(Grammar grammar){
        grammarRepository.delete(grammar);
    }
}