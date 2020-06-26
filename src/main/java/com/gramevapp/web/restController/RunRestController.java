package com.gramevapp.web.restController;

import com.gramevapp.web.model.Run;
import com.gramevapp.web.model.RunDto;
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
    @ResponseBody
    public RunDto getRunStatus(String runId, String status) {

        if (runId.equals(""))
            return null;

        Run run = runService.findByRunId(Long.parseLong(runId));

        run.setCurrentGeneration(run.getDiagramData() != null ? run.getDiagramData().getCurrentGeneration() : 0);
        run.setBestIndividual(run.getDiagramData() != null ? run.getDiagramData().getBestIndividual() : 0.0);

        return new RunDto(run);
    }


}
