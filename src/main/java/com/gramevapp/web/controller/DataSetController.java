package com.gramevapp.web.controller;

import com.gramevapp.web.model.ExperimentDataType;
import com.gramevapp.web.model.Grammar;
import com.gramevapp.web.model.User;
import com.gramevapp.web.repository.ExperimentDataTypeRepository;
import com.gramevapp.web.service.ExperimentService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
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

        model.addAttribute("datasetList", datasetList);

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
                return Long.valueOf(-1);
            }
        }

        return idDataset;
    }
}
