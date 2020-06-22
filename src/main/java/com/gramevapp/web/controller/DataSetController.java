package com.gramevapp.web.controller;

import com.gramevapp.web.model.Dataset;
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

@Controller
public class DataSetController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExperimentService experimentService;

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

        for (Dataset experimentDataType : datasetList) {
            disabled.add(experimentDataTypeListInUse.contains(experimentDataType));
            StringBuilder stringBuilder = new StringBuilder();
            String info = experimentDataType.getInfo();
            List<String> currentDataset = new ArrayList<>();
            if (info != null && !info.equals("")) {
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
            datasetInformation.add(currentDataset);
        }

        model.addAttribute("datasetList", datasetList);
        model.addAttribute("datasetListDisabled", disabled);
        model.addAttribute("datasetInformationList", datasetInformation);

        return "dataset/datasetRepository";
    }

    @RequestMapping(value = "/dataset/datasetDetail")
    public String createDataset(Model model) {
        User user = userService.getLoggedInUser();
        model.addAttribute("experimentDataType", new Dataset());
        model.addAttribute("user", user);
        return "dataset/datasetDetail";
    }

    @RequestMapping(value = "/dataset/saveDataset", method = RequestMethod.POST)
    public String saveDataset(Model model, @ModelAttribute("experimentDataType") @Valid Dataset experimentDataType,
                              @Param("checkFold") String checkFold, @Param("kFoldNumber") int kFoldNumber) {
        experimentDataType.setDataTypeType("training");
        experimentDataType.setCreationDate(new Timestamp(new Date().getTime()));
        experimentDataType.setUserIdUserId(userService.getLoggedInUser().getId());
        if (checkFold != null && checkFold.equals("true")) {
            foldDataset(experimentDataType, kFoldNumber);
        }
        experimentService.saveDataType(experimentDataType);
        return datasetList(model);
    }

    @RequestMapping(value = "/dataset/deleteDataset", method = RequestMethod.POST, params = "deleteDataset")
    @ResponseBody
    public Long expRepoSelectedDelete(@RequestParam("datasetId") String datasetId) {
        Long idDataset = Long.parseLong(datasetId);
        try {
            experimentService.deleteDataTypeFile(idDataset);
        } catch (DataIntegrityViolationException e) {
            System.out.println(e.getCause() instanceof org.hibernate.exception.ConstraintViolationException);
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                return (long) -1;
            }
        }

        return idDataset;
    }
    @RequestMapping(value = "/foldDataset", method = RequestMethod.POST)
    @ResponseBody
    public String ajaxFoldDataset(@RequestParam("datasetId") String datasetId,@RequestParam("kFoldNumber") int kFoldNumber) {
        Dataset dataset = experimentService.findExperimentDataTypeById( Long.parseLong(datasetId));
        foldDataset(dataset, kFoldNumber);
        experimentService.saveDataType(dataset);
        return dataset.getInfo();
    }

    private void foldDataset(Dataset experimentDataType,int kFoldNumber){
        List<Integer> kFoldValues = new ArrayList<>();
        for (int i = 0; i < kFoldNumber; i++) {
            kFoldValues.add(i);
        }
        Collections.shuffle(kFoldValues);
        String[] splitDatasetInfo = experimentDataType.getInfo().split("\\r\\n");
        String newDataSetInfo = "";
        newDataSetInfo += splitDatasetInfo[0] + ";K-Fold\r\n";
        for (int i = 1; i < splitDatasetInfo.length; i++) {
            if (!splitDatasetInfo[i].equals("")) {
                newDataSetInfo += splitDatasetInfo[i] + ";" + kFoldValues.get(i % kFoldValues.size()) + "\r\n";
            }
        }
        experimentDataType.setInfo(newDataSetInfo);
    }
}
