package com.gramevapp.web.service;

import com.gramevapp.web.model.*;
import com.gramevapp.web.repository.ExperimentDataTypeRepository;
import com.gramevapp.web.repository.ExperimentRepository;
import com.gramevapp.web.repository.ExperimentRowTypeRepository;
import com.gramevapp.web.repository.GrammarRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// We use repositories ExperimentDataType, ExperimentRowType here too.
@Service
public class ExperimentService {

    // Test path
    private static final String SAMPLE_CSV_FILE_PATH = "resources/csv/EjemBasico.csv";

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private GrammarRepository grammarRepository;

    @Autowired
    private ExperimentDataTypeRepository experimentDataTypeRepository;

    @Autowired
    private ExperimentRowTypeRepository experimentRowTypeRepository;

    // Load all the data of ExperimentDataTypeFile (Id, userId, name, description/status, modificationDate, List<ExpRowType>,..)
    public void loadExperimentDataTypeFile() throws IOException{


        Reader reader = Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE_PATH));
        //loadExperimentRowTypeFile(reader);
    }


    // Add ExperimentDataType file into the DD.BB. - Just the Validation, Test, Training text
    // - This means read line by line the file, create a ExperimentRowType by line,
    // add the line in the list and upload the row in the DDBB
    public void loadExperimentRowTypeFile(Reader fileTypeReader, ExperimentDataType expDataType){

            try {
                CsvToBean csvToBean = new CsvToBeanBuilder(fileTypeReader)
                        .withType(ExperimentRowType.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();

                List<ExperimentRowType> expRowsType = csvToBean.parse();

                for (ExperimentRowType expRowType : expRowsType) {
                    ExperimentRowType expRow = expDataType.addExperimentRowType(expRowType);

                    experimentRowTypeRepository.save(expRow);

                    System.out.println("Id Row : " + expRowType.getId());
                    System.out.println("String row : " + expRowType.getRowString());
                    System.out.println("---------------------------");
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
    }

    public ExperimentDataType saveDataType(ExperimentDataType expDataType){
        return experimentDataTypeRepository.save(expDataType);
    }

    public void saveGrammar(Grammar grammar){
        grammarRepository.save(grammar);
    }

    public void saveExperiment(Experiment experiment){
        experimentRepository.save(experiment);
    }

    public List<Experiment> findByUserId(User user) {
        return experimentRepository.findByUserId(user);
    }

    public Experiment findExperimentByUserIdAndName(User user, String nameExp){
        return experimentRepository.findByUserIdAndExperimentName(user, nameExp);
    }

    public Experiment findExperimentByUserIdAndExpId(User user, Long expId){
        return experimentRepository.findByUserIdAndId(user, expId);
    }

    public Grammar findGrammarByUserIdAndName(User user, String nameExp){
        return grammarRepository.findByUserIdAndGrammarName(user, nameExp);
    }

    public ExperimentDataType findDataTypeByUserIdAndName(User user, String nameExp){
        return experimentDataTypeRepository.findByUserIdAndDataTypeName(user, nameExp);
    }

    public Experiment findExperimentById(Long id)
    {
        return experimentRepository.findById(id);
    }

    public ExperimentDataType findExperimentDataTypeById(Long id){
        return experimentDataTypeRepository.findById(id);
    }

}