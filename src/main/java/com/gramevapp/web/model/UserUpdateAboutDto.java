package com.gramevapp.web.model;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;

public class UserUpdateAboutDto {

    private static final String PATTERN_NUM_LETTERS = "^[\\p{L} \\d .'-]+$";

    private Long id;

    @Pattern(regexp = PATTERN_NUM_LETTERS, message = "Your user information cannot contain strange characters")
    private String aboutMe;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }
}