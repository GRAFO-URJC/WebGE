package com.gramevapp.web.restController;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.model.User;
import com.gramevapp.web.service.DiagramDataService;
import com.gramevapp.web.service.ExperimentService;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
//@Controller
public class DiagramDataRestController {

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private UserService userService;

    @Autowired
    private DiagramDataService diagramDataService;

    @Autowired
    private RunService runService;

    @RequestMapping(value = "/user/rest/diagramFlow/{idRun}", method = RequestMethod.GET,
            produces = "application/json")
    public @ResponseBody
    DiagramData getLastBestIndividual(@PathVariable("idRun") String runId) {
        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
        }

        Long runIdLong = Long.parseLong(runId);
        Run run = runService.findByUserIdAndRunId(user, runIdLong);
        DiagramData diagramData = diagramDataService.getLastBestIndividual(run);
        return diagramData;
    }

    /*@ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CountryNotFoundException.class)
    public void countryNotFound() {
    }*/
}