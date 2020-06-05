package com.gramevapp.web.controller;

import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.ExperimentDataType;
import com.gramevapp.web.model.User;
import com.gramevapp.web.service.ExperimentService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Controller
public class DataSetController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExperimentService experimentService;

    @GetMapping("/datasets/list")
    public String datasetList(Model model) {
        User user = userService.getLoggedInUser();
        List<ExperimentDataType> datasetList =
                experimentService.findAllExperimentDataTypeByUserId(user.getId());
        List<Boolean> disabled = new ArrayList<>();
        List<List<String>> datasetInformation = new ArrayList<>();
        HashSet<ExperimentDataType> experimentDataTypeListInUse = new HashSet<>();

        List<Experiment> experimentList = experimentService.findByUser(user);
        for (Experiment experiment : experimentList) {
            if (experiment.getDefaultExpDataType() != null) {
                experimentDataTypeListInUse.add(experimentService.findExperimentDataTypeById(experiment.getDefaultExpDataType()));
            }
            if (experiment.getDefaultTestExpDataTypeId() != null) {
                experimentDataTypeListInUse.add(experimentService.findExperimentDataTypeById(experiment.getDefaultTestExpDataTypeId()));
            }
        }

        for (ExperimentDataType experimentDataType : datasetList) {
            disabled.add(experimentDataTypeListInUse.contains(experimentDataType));
            StringBuilder stringBuilder = new StringBuilder();
            String info = experimentDataType.getinfo();
            List<String> currentDataset = new ArrayList<>();
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
        model.addAttribute("experimentDataType", new ExperimentDataType());
        model.addAttribute("user", user);
        return "dataset/datasetDetail";
    }

    @RequestMapping(value = "/dataset/saveDataset", method = RequestMethod.POST)
    public String saveGrammar(Model model, @ModelAttribute("experimentDataType") @Valid ExperimentDataType experimentDataType) {
        experimentDataType.setDataTypeType("training");
        experimentDataType.setCreationDate(new Date(System.currentTimeMillis()));
        experimentDataType.setUserIdUserId(userService.getLoggedInUser().getId());
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
}
