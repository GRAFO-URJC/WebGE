package com.gramevapp.web.model;

import javax.validation.constraints.Pattern;

public class UserUpdateStudyDto {

    private static final String PATTERN_NUM_LETTERS = "^[\\p{L} \\d .'-]*$";

    @Pattern(regexp = PATTERN_NUM_LETTERS, message = "The study information cannot contain strange characters")
    private String studyInformation;
    @Pattern(regexp = PATTERN_NUM_LETTERS, message = "The work information cannot contain strange characters")
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