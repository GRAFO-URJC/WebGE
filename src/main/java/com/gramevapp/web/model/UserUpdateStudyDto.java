package com.gramevapp.web.model;

import javax.validation.constraints.AssertTrue;

public class UserUpdateStudyDto {

    private Long id;

    // This must best a list<String> listStudy and listWork, but for now... This is ok
    private String studyInformation;
    private String workInformation;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getStudyInformation() {
        return studyInformation;
    }

    public void setStudyInformation(String studyInformation) {
        this.studyInformation = studyInformation;
    }

    public String getWorkInformation() {
        return workInformation;
    }

    public void setWorkInformation(String workInformation) {
        this.workInformation = workInformation;
    }
}