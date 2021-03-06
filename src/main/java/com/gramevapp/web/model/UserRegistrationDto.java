package com.gramevapp.web.model;

import com.gramevapp.config.FieldMatch;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@FieldMatch.List({
        @FieldMatch(first = "password", second = "confirmPassword", message = "The password fields must match"),
        @FieldMatch(first = "confirmPassword", second = "password", message = "The password fields must match"),
        @FieldMatch(first = "email", second = "confirmEmail", message = "The email fields must match"),
        @FieldMatch(first = "confirmEmail", second = "email", message = "The email fields must match")
})

public class UserRegistrationDto {
    private static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";
    private static final String EMAIL_PATTERN= "^[^@]+@[^@]+$";

    @Pattern(regexp = USERNAME_PATTERN, message = "Username cannot have spaces neither strange characters or uppercase and must have between 3-15 letters")
    @NotNull(message = "User name cannot be empty")
    private String username;

    @NotNull
    @Size(min = 1, message = "Firstname cannot be empty")
    private String firstName;

    @NotNull
    @Size(min = 1,message = "Lastname cannot be empty")
    private String lastName;

    @NotNull
    @Size(min = 6, max = 50, message = "Your password must between 6 and 15 characters")
    private String password;

    @NotNull
    @Size(min = 6, max = 50, message = "Your confirmation password must between 6 and 15 characters")
    private String confirmPassword;

    @NotNull
    @Pattern(regexp = EMAIL_PATTERN, message = "Not valid email")
    private String email;

    @NotNull
    @Pattern(regexp = EMAIL_PATTERN, message = "Not valid email")
    private String confirmEmail;

    @NotNull
    @Size(min = 1, message = "Institution cannot be empty")
    private String institution;

    //  Direction
    private String address;
    private String city;
    private String state;
    private Integer zipcode;

    // Work / Study
    private String studyInformation;
    private String workInformation;

    // Extra info
    private String aboutMe;

    @AssertTrue(message = "You need to agree accept terms and conditions")
    private Boolean terms;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getZipcode() {
        return zipcode;
    }

    public void setZipcode(Integer zipcode) {
        this.zipcode = zipcode;
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

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getConfirmEmail() {
        return confirmEmail;
    }

    public void setConfirmEmail(String confirmEmail) {
        this.confirmEmail = confirmEmail;
    }

    public Boolean getTerms() {
        return terms;
    }

    public void setTerms(Boolean terms) {
        this.terms = terms;
    }


    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }
}