package com.gramevapp.web.model;

import com.gramevapp.config.FieldMatch;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Size;

@FieldMatch.List({
        @FieldMatch(first = "password",
                second = "confirmPassword", message = "The password fields must match")
})

public class UserUpdatePasswordDto {
    @NotEmpty(message = "The old password is incorrect")
    private String oldPassword;

    @NotEmpty
    @Size(min = 6, max = 50, message = "Your new password must between 6 and 15 characters")
    private String password;

    @NotEmpty
    @Size(min = 6, max = 50, message = "Your new password must between 6 and 15 characters")
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

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
}