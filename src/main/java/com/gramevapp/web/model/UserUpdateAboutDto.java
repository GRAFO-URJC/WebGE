package com.gramevapp.web.model;

import javax.validation.constraints.AssertTrue;

public class UserUpdateAboutDto {

    private Long id;

    private String aboutMe;

    @AssertTrue
    private Boolean terms;

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