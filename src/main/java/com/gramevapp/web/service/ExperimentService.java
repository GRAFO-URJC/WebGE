package com.gramevapp.web.service;

import com.gramevapp.web.model.*;
import com.gramevapp.web.repository.*;
import com.opencsv.bean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.Reader;
import java.util.List;
import java.util.UUID;

// We use repositories ExperimentDataType, ExperimentRowType here too.
@Service
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

        /*try {
            //csv file containing data
            CSVReader reader = new CSVReader(fileTypeReader);
            String [] nextLine;
            int lineNumber = 0;
            while ((nextLine = reader.readNext()) != null) {
                lineNumber++;
                System.out.println("Line # " + lineNumber);

                // nextLine[] is an array of values from the line
                System.out.println(nextLine[4] + "etc...");
            }
        } */

        /* lo anterior hay q pensarlo
                en la clase q vaya a tener el modelo de csv que tenga una lista con los atributos. De esta manera será dinamico
                Mirar supercsv para ver si resolvemos el problema */


        ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
        strategy.setType(ExperimentRowType.class);
        String[] fields = {"Y", "X1", "X2", "X3", "X4", "X5", "X6", "X7", "X8", "X9", "X10"};
        strategy.setColumnMapping(fields);

        try {
            CsvToBean csvToBean = new CsvToBeanBuilder(fileTypeReader)
                    .withSeparator(';')
                    .withMappingStrategy(strategy)
                    .withType(ExperimentRowType.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<ExperimentRowType> expRowsType = csvToBean.parse();

            //expRowsType.forEach(System.out::println);

            for (ExperimentRowType expRowType : expRowsType) {
                ExperimentRowType expRow = expDataType.addExperimentRowType(expRowType);

                experimentRowTypeRepository.save(expRow);

                /*System.out.println("# Y Custom : " + expRowType.getYCustom());
                System.out.println("X1 : " + expRowType.getX1());
                System.out.println("---------------------------");*/
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

    public ExpProperties findPropertiesById(UUID propertiesId){
        return propertiesRepository.findById(propertiesId);
    }

}