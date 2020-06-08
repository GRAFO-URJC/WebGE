package com.gramevapp.web.model;

import javax.persistence.*;

@Entity
@Table(name = "user_details")
public class UserDetails {

    @Id
    @Column(name = "USER_ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    private User user;

    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private Integer phone;

    //  Direction
    @Column
    private String addressDirection;
    @Column
    private String city;
    @Column
    private String state;
    @Column
    private Integer zipcode;

    // Work / Study
    @Column
    private String studyInformation;
    @Column
    private String workInformation;

    // Extra info
    @Column
    private String aboutMe = " ";


    @JoinColumn(name = "PROFILE_PICTURE_ID", unique = true)
    @OneToOne(cascade = CascadeType.ALL)
    private UploadFile profilePicture;

    public UserDetails() {
    }

    public UserDetails(User user, String firstName, String lastName, Integer phone, String addressDirection, String city, String state, Integer zipcode, String studyInformation, String workInformation, String aboutMe, UploadFile profilePicture) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.addressDirection = addressDirection;
        this.city = city;
        this.state = state;
        this.zipcode = zipcode;
        this.studyInformation = studyInformation;
        this.workInformation = workInformation;
        this.aboutMe = aboutMe;
        this.profilePicture = profilePicture;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {
        this.phone = phone;
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

    public UploadFile getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(UploadFile profilePicture) {
        this.profilePicture = profilePicture;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}