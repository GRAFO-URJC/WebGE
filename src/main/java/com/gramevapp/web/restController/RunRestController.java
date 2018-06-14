package com.gramevapp.web.restController;

import com.gramevapp.web.model.Run;
import com.gramevapp.web.model.User;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController("runRestController")
public class RunRestController {

    @Autowired
    UserService userService;

    @Autowired
    RunService runService;

    @RequestMapping(value = "/user/rest/runStatus/", method = RequestMethod.GET,
            produces = "application/json")
    public @ResponseBody
    Run getRunStatus(String runId, String status) {

        User user = userService.getLoggedInUser();
        if(user == null)
            System.out.println("User not authenticated");

        if(runId == "")
            return null;

        Run run = runService.findByRunId(Long.parseLong(runId));
        run.setCurrentGeneration(run.getDiagramData().getCurrentGeneration());
        run.setBestIndividual(run.getDiagramData().getBestIndividual());

        if(status.equals("INITIALIZING")) {
            run.setStatus(Run.Status.RUNNING);

            run.setBestIndividual(1.0);
            run.setCurrentGeneration(0);

            run.getDiagramData().setFinished(false);
            run.getDiagramData().setBestIndividual(1.0);
            run.getDiagramData().setCurrentGeneration(0);

            run.getDiagramData().setListPair(new ArrayList<>());
        }

        if(run.getDiagramData().getFinished() || run.getDiagramData().getBestIndividual() <= 0.0)
            run.setStatus(Run.Status.FINISHED);

        return run;
    }

}
