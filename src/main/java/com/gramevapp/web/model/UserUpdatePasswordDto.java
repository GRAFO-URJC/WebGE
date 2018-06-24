package com.gramevapp.web.model;

import com.gramevapp.config.FieldMatch;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@FieldMatch.List({
        @FieldMatch(first = "password", second = "confirmPassword", message = "The password fields must match"),
})

public class UserUpdatePasswordDto {
    @NotEmpty
    @Size(min = 6, max = 50, message = "Your password must between 6 and 15 characters")
    private String password;

    @NotEmpty
    @Size(min = 6, max = 50, message = "Your password must between 6 and 15 characters")
    private String confirmPassword;

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

}