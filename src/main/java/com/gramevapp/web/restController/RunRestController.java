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
        if (user == null)
            System.out.println("User not authenticated");

        if (runId == "")
            return null;

        Run run = runService.findByRunId(Long.parseLong(runId));
        run.setCurrentGeneration(run.getDiagramData().getCurrentGeneration());
        run.setBestIndividual(run.getDiagramData().getBestIndividual());

        if (run.getDiagramData().getFinished() || run.getDiagramData().getBestIndividual() <= 0.0) {
            if (run.getDiagramData().getBestIndividual() <= 0.0) {
                run.setBestIndividual(0.0);
            }
            this.setStatus(run, Run.Status.FINISHED);
        }

        if (run.getDiagramData().getStopped()) {
            this.setStatus(run, Run.Status.STOPPED);
        }
        if (run.getDiagramData().getFailed()) {
            this.setStatus(run, Run.Status.FAILED);
        }

        return run;
    }

    private void setStatus(Run run, Run.Status runStatus) {
        run.setStatus(runStatus);
        runService.saveRun(run);
    }

}
