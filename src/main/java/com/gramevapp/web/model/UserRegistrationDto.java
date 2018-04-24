package com.gramevapp.web.model;

import com.gramevapp.config.FieldMatch;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@FieldMatch.List({
        @FieldMatch(first = "password", second = "confirmPassword", message = "The password fields must match"),
        @FieldMatch(first = "email", second = "confirmEmail", message = "The email fields must match")
})



public class UserRegistrationDto {

    private Long id;

    private static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";


    @Pattern(regexp = USERNAME_PATTERN, message = "Username cannot have spaces neither strange characters or uppercase")
    @NotEmpty(message = "User name cannot be empty")
    private String username;

    @NotEmpty(message = "Firstname cannot be empty")
    private String FirstName;

    @NotEmpty(message = "Lastname cannot be empty")
    private String LastName;

    @NotEmpty
    @Size(min = 6, max = 15, message = "Your password must between 6 and 15 characters")
    private String password;

    @NotEmpty
    @Size(min = 6, max = 15, message = "Your confirmation password must between 6 and 15 characters")
    private String confirmPassword;

    @Email
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @Email
    @NotEmpty(message = "Email confirmation cannot be empty")
    private String confirmEmail;

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

    @AssertTrue
    private Boolean terms;

    public Long getId(){ return id; }

    public void setId(Long id){ this.id = id; }

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
        return FirstName;
    }

    public void setFirstName(String firstName) {
        this.FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        this.LastName = lastName;
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

}