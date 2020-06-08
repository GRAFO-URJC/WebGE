package com.gramevapp.web.restController;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.model.User;
import com.gramevapp.web.service.DiagramDataService;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController("diagramDataRestController")
public class DiagramDataRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private DiagramDataService diagramDataService;

    @Autowired
    private RunService runService;

    @RequestMapping(value = "/user/rest/diagramFlow/", method = RequestMethod.GET,
            produces = "application/json")
    public @ResponseBody
    DiagramData getLastBestIndividual(String runId) {
        User user = userService.getLoggedInUser();
        if (user == null)
            System.out.println("User not authenticated");
        Long longRunId = Long.parseLong(runId);
        Run run = runService.findByRunId(longRunId);

        DiagramData diagramData = diagramDataService.getLastBestIndividual(run);
        diagramData.getListPair().sort((o1, o2) -> {
            if (o1.getCurrentGeneration() > o2.getCurrentGeneration()) {
                return 1;
            } else if (o1.getCurrentGeneration() == o2.getCurrentGeneration()) {
                return 0;
            }
            return -1;
        });
        return diagramData;
    }
}