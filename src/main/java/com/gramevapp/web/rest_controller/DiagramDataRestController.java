package com.gramevapp.web.rest_controller;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.DiagramDataDto;
import com.gramevapp.web.service.DiagramDataService;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/user/rest/diagramFlow/", produces = "application/json")
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