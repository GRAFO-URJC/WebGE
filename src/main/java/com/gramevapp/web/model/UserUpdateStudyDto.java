package com.gramevapp.web.model;

public class UserUpdateStudyDto {

    private String studyInformation;
    private String workInformation;

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