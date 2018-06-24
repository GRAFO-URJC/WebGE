package com.gramevapp.web.model;

import javax.validation.constraints.Pattern;

public class UserUpdateAboutDto {

    private static final String PATTERN_NUM_LETTERS = "^[\\p{L} \\d .'-]*$";

    @Pattern(regexp = PATTERN_NUM_LETTERS, message = "Your user information cannot contain strange characters")
    private String aboutMe;

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }
}