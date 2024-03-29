package com.gramevapp.web.controller;

import com.gramevapp.web.model.Dataset;
import com.gramevapp.web.model.DatasetDTO;
import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.User;
import com.gramevapp.web.service.ExperimentService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Logger;

@Controller
public class DataSetController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExperimentService experimentService;


    Logger logger = Logger.getLogger(DataSetController.class.getName());
    @GetMapping("/datasets/list")
    public String datasetList(Model model) {
        User user = userService.getLoggedInUser();
        List<Dataset> datasetList =
                experimentService.findAllExperimentDataTypeByUserId(user.getId());
        List<Boolean> disabled = new ArrayList<>();
        List<List<String>> datasetInformation = new ArrayList<>();
        HashSet<Dataset> experimentDataTypeListInUse = new HashSet<>();

        List<Experiment> experimentList = experimentService.findByUser(user);
        for (Experiment experiment : experimentList) {
            if (experiment.getDefaultExpDataType() != null) {
                experimentDataTypeListInUse.add(experimentService.findExperimentDataTypeById(experiment.getDefaultExpDataType()));
            }
            if (experiment.getDefaultTestExpDataTypeId() != null) {
                experimentDataTypeListInUse.add(experimentService.findExperimentDataTypeById(experiment.getDefaultTestExpDataTypeId()));
            }
        }
        setDatasetInfo(datasetList, disabled, datasetInformation, experimentDataTypeListInUse);

        model.addAttribute("datasetList", datasetList);
        model.addAttribute("datasetListDisabled", disabled);
        model.addAttribute("datasetInformationList", datasetInformation);

        return "dataset/datasetRepository";
    }

    private void setDatasetInfo(List<Dataset> datasetList, List<Boolean> disabled, List<List<String>> datasetInformation, HashSet<Dataset> experimentDataTypeListInUse) {
        for (Dataset experimentDataType : datasetList) {
            disabled.add(experimentDataTypeListInUse.contains(experimentDataType));
            StringBuilder stringBuilder = new StringBuilder();
            String info = experimentDataType.getInfo();
            List<String> currentDataset = new ArrayList<>();
            if (info != null && !info.equals("")) {
                generateInfo(stringBuilder, info, currentDataset);
            }
            datasetInformation.add(currentDataset);
        }
    }

    private void generateInfo(StringBuilder stringBuilder, String info, List<String> currentDataset) {
        int maxSize = info.split("\\n").length / 35;
        if (maxSize == 0) {
            maxSize = 12;
        }
        int count = 0;
        for (int i = 0; i < info.length(); i++) {
            if (info.charAt(i) == '\n') {
                count++;
            } else {
                stringBuilder.append(info.charAt(i));
            }
            if (count == maxSize) {
                currentDataset.add(stringBuilder.toString());
                count = 0;
                stringBuilder = new StringBuilder();
            }
        }
        if (count != 0 && !stringBuilder.toString().equals("\r") && !stringBuilder.toString().equals("\n")) {
            currentDataset.add(stringBuilder.toString());
        }
    }

    @RequestMapping(value = "/dataset/datasetDetail")
    public String createDataset(Model model, Boolean existed) {
        User user = userService.getLoggedInUser();
        model.addAttribute("experimentDataType", new Dataset());
        model.addAttribute("user", user);
        model.addAttribute("existed", existed);
        return "dataset/datasetDetail";
    }

    @PostMapping(value = "/dataset/saveDataset")
    public String saveDataset(Model model, @ModelAttribute("experimentDataType") @Valid DatasetDTO datasetDto,
                              @Param("checkFold") String checkFold, @Param("kFoldNumber") int kFoldNumber) {

        User user = userService.getLoggedInUser();
        Dataset experimentDataType = new Dataset(datasetDto);

        if(experimentService.findExperimentDataTypeByDataTypeNameAndUserId(experimentDataType.getDataTypeName(), user.getId())== null) {

            experimentDataType.setDataTypeType("training");
            experimentDataType.setCreationDate(new Timestamp(new Date().getTime()));
            experimentDataType.setUserIdUserId(userService.getLoggedInUser().getId());
            if (experimentDataType.getInfo().contains("K-Fold")) {
                HashSet<Integer> listFoldSize = new HashSet<>();
                String[] rows = experimentDataType.getInfo().split("\r\n");

                int indexFold;
                for (int i = 1; i < rows.length; i++) {
                    indexFold = rows[i].lastIndexOf(';');
                    listFoldSize.add(Integer.parseInt(rows[i].substring(indexFold + 1)));
                }
                kFoldNumber = listFoldSize.size();
                checkFold = "true";
            }
            if (checkFold != null && checkFold.equals("true")) {
                foldDataset(experimentDataType, kFoldNumber);
                experimentDataType.setFoldSize(kFoldNumber);
            }
            experimentService.saveDataType(experimentDataType);
            return datasetList(model);
        }
        return createDataset(model, true);

    }

    @PostMapping(value = "/dataset/deleteDataset", params = "deleteDataset")
    @ResponseBody
    public Long expRepoSelectedDelete(@RequestParam("datasetId") String datasetId) {
        Long idDataset = Long.parseLong(datasetId);
        try {
            experimentService.deleteDataTypeFile(idDataset);
        } catch (DataIntegrityViolationException e) {
            logger.warning(String.valueOf(e.getCause() instanceof org.hibernate.exception.ConstraintViolationException));
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                return (long) -1;
            }
        }

        return idDataset;
    }

    @PostMapping(value = "/foldDataset")
    @ResponseBody
    public String ajaxFoldDataset(@RequestParam("datasetId") String datasetId, @RequestParam("kFoldNumber") int kFoldNumber) {
        Dataset dataset = experimentService.findExperimentDataTypeById(Long.parseLong(datasetId));
        foldDataset(dataset, kFoldNumber);
        experimentService.saveDataType(dataset);
        return dataset.getInfo();
    }

    private void foldDataset(Dataset experimentDataType, int kFoldNumber) {
        List<Integer> kFoldValues = new ArrayList<>();
        for (int i = 1; i <= kFoldNumber; i++) {
            kFoldValues.add(i);
        }
        experimentDataType.setFoldSize(kFoldNumber);
        Collections.shuffle(kFoldValues);
        String[] splitDatasetInfo = experimentDataType.getInfo().split("\\r\\n");
        String newDataSetInfo = "";
        newDataSetInfo += splitDatasetInfo[0] + ";K-Fold\r\n";
        for (int i = 1; i < splitDatasetInfo.length; i++) {
            if (!splitDatasetInfo[i].equals("")) {
                StringBuilder stringBuilder = new StringBuilder(newDataSetInfo);
                stringBuilder.append(splitDatasetInfo[i] );
                stringBuilder.append(";");
                stringBuilder.append(kFoldValues.get(i % kFoldValues.size()));
                stringBuilder.append("\r\n");
                newDataSetInfo = String.valueOf(stringBuilder);
            }
        }
        experimentDataType.setInfo(newDataSetInfo);
    }

    ////////
    @PostMapping(value = "/getDataset")
    @ResponseBody
    public String getDatasetContent(@RequestParam("datasetId") String datasetId) {
        Dataset dataset = experimentService.findExperimentDataTypeById(Long.parseLong(datasetId));
        return dataset.getInfo();
    }

}
