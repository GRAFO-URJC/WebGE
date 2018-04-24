package com.gramevapp.web.model;

import com.gramevapp.config.FieldMatch;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.AssertTrue;

@FieldMatch.List({
        @FieldMatch(first = "password", second = "confirmPassword", message = "The password fields must match"),
})

public class UserUpdatePasswordDto {

    private Long id;

    @NotEmpty
    private String password;

    @NotEmpty
    private String confirmPassword;

    @AssertTrue
    private Boolean terms;

    public Long getId(){ return id; };

    public void setId(Long id){ this.id = id; };

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Boolean getTerms() {
        return terms;
    }

    public void setTerms(Boolean terms) {
        this.terms = terms;
    }
}