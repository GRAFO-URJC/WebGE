package com.gramevapp.web.restController;

import com.gramevapp.web.model.Run;
import com.gramevapp.web.model.User;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("runRestController")
public class RunRestController {

    @Autowired
    UserService userService;

    @Autowired
    RunService runService;

    @RequestMapping(value = "/user/rest/runStatus/{runId}", method = RequestMethod.GET,
            produces = "application/json")
    public @ResponseBody
    Run getRunStatus(@PathVariable("runId") String runId) {

        User user = userService.getLoggedInUser();
        if(user == null)
            System.out.println("User not authenticated");

        Run run = runService.findByRunId(Long.parseLong(runId));
        run.setCurrentGeneration(run.getDiagramData().getCurrentGeneration());
        run.setBestIndividual(run.getDiagramData().getBestIndividual());

        if(run.getDiagramData().getFinished() ){
            run.setStatus(Run.Status.FINISHED);
        }

        return run;
    }

}
