package com.gramevapp.web.restController;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.DiagramDataDto;
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

import java.util.ArrayList;
import java.util.List;


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
    @ResponseBody
    public List<DiagramDataDto> getDiagramDataInfo(String runId, int count) {
        List<DiagramData> diagramDataList = runService.findByRunId(Long.parseLong(runId)).getDiagramDataList();
        List<DiagramDataDto> diagramDataDtoList = new ArrayList<>();
        for(;count<diagramDataList.size();count++){
            diagramDataDtoList.add(new DiagramDataDto(diagramDataList.get(count)));
        }
        return diagramDataDtoList;
    }
}