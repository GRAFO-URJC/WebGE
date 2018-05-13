package com.gramevapp.web.model;

import javax.validation.constraints.Pattern;

public class UserUpdateStudyDto {

    private static final String PATTERN_NUM_LETTERS = "^[\\p{L} \\d .'-]+$";

    private Long id;

    // This must best a list<String> listStudy and listWork, but for now... This is ok
    @Pattern(regexp = PATTERN_NUM_LETTERS, message = "The study information cannot contain strange characters")
    private String studyInformation;
    @Pattern(regexp = PATTERN_NUM_LETTERS, message = "The work information cannot contain strange characters")
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