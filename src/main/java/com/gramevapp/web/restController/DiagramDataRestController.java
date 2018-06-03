package com.gramevapp.web.restController;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.model.User;
import com.gramevapp.web.service.DiagramDataService;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("diagramDataRestController")
public class DiagramDataRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private DiagramDataService diagramDataService;

    @Autowired
    private RunService runService;

    /*@RequestMapping(value = "/user/rest/diagramFlow/{runId}", method = RequestMethod.GET,
            produces = "application/json")
    public @ResponseBody
    DiagramData getLastBestIndividual(@PathVariable("runId") String runId) {
        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
        }

        /*if(runId.equals("undefined")){
            return new DiagramData(0.0);
        }*/
        /*
        System.out.println(runId);

        // Long runIdLong = Long.parseLong(runId);
        Run run = runService.findLastRunId();
        // Run run = runService.findByUserIdAndRunId(user, runIdLong);
        DiagramData diagramData = diagramDataService.getLastBestIndividual(run);
        return diagramData;
    }*/

    @RequestMapping(value = "/user/rest/diagramFlow/{runId}", method = RequestMethod.GET,
            produces = "application/json")
    public @ResponseBody
    DiagramData getLastBestIndividual(@PathVariable("runId") String runId) {
        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
        }

        Long longRunId = Long.parseLong(runId);

        DiagramData diagramData = diagramDataService.getLastBestIndividual(longRunId);
        return diagramData;
    }
}