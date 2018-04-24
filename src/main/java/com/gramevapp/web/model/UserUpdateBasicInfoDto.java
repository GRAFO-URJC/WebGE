package com.gramevapp.web.model;

import javax.validation.constraints.AssertTrue;

public class UserUpdateBasicInfoDto {

    private Long id;

    //  Basic info
    private String FirstName;
    private String LastName;
    private String email;
    private Integer phone;

    //  Direction
    private String addressDirection;
    private String city;
    private String state;
    private Integer zipcode;

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