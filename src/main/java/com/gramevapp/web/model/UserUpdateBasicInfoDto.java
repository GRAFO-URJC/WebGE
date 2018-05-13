package com.gramevapp.web.model;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;

public class UserUpdateBasicInfoDto {

    private static final String PATTERN = "^[\\p{L} .'-]+$";  // https://stackoverflow.com/questions/15805555/java-regex-to-validate-full-name-allow-only-spaces-and-letters
    private static final String PATTERN_NUM_LETTERS = "^[\\p{L} \\d .'-]+$";

    private Long id;

    //  Basic info
    @Pattern(regexp = PATTERN, message = "First name cannot contain strange characters")
    @NotEmpty
    private String firstName;
    @Pattern(regexp = PATTERN, message = "Last name cannot contain strange characters")
    @NotEmpty
    private String lastName;
    @Email
    @NotEmpty
    private String email;
    @NumberFormat
    private Integer phone;

    //  Direction
    @Pattern(regexp = PATTERN_NUM_LETTERS, message = "Address direction cannot contain strange characters")
    private String addressDirection;
    @Pattern(regexp = PATTERN, message = "City direction cannot contain strange characters")
    private String city;
    @Pattern(regexp = PATTERN, message = "State direction cannot contain strange characters")
    private String state;
    @NumberFormat
    private Integer zipcode;

    private MultipartFile profilePicture;

    @AssertTrue
    private Boolean terms;

    public Long getId() {
        return id;
    }

    ;

    public void setId(Long id) {
        this.id = id;
    }

    ;

    public MultipartFile getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(MultipartFile profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getAddressDirection() {
        return addressDirection;
    }

    public void setAddressDirection(String addressDirection) {
        this.addressDirection = addressDirection;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getTerms() {
        return terms;
    }

    public void setTerms(Boolean terms) {
        this.terms = terms;
    }

    public Integer getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {
        this.phone = phone;
    }
}