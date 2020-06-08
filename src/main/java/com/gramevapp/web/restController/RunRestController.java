package com.gramevapp.web.restController;

import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("runRestController")
public class RunRestController {

    @Autowired
    UserService userService;

    @Autowired
    RunService runService;

    @RequestMapping(value = "/rest/runStatus/", method = RequestMethod.GET,
            produces = "application/json")
    public @ResponseBody
    Run getRunStatus(String runId, String status) {

        if (runId.equals(""))
            return null;

        Run run = runService.findByRunId(Long.parseLong(runId));
        run.setCurrentGeneration(run.getDiagramData().getCurrentGeneration());
        run.setBestIndividual(run.getDiagramData().getBestIndividual());

        if (run.getDiagramData().getFinished() || run.getDiagramData().getBestIndividual() <= 0.0 && !status.equals("WAITING")) {
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
