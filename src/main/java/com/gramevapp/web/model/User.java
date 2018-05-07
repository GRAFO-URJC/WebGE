package com.gramevapp.web.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
public class User implements Serializable {

    @Id
    @Column(name = "USER_ID", nullable = false, updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column(nullable = false)
    private String email;
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
    private String aboutMe =" ";

    @Column
    private Boolean enabled = true;
    @Column
    private Integer failedLoginAttempts = 0;

    @ManyToMany(cascade=CascadeType.PERSIST, fetch=FetchType.EAGER, targetEntity=Role.class)
    @JoinTable(
            name = "users_roles",
            joinColumns ={
                    @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
                    @JoinColumn(name = "username", referencedColumnName = "username"),
                    @JoinColumn(name = "email", referencedColumnName = "email")
            },
            inverseJoinColumns ={
                    @JoinColumn(name = "role_id", referencedColumnName = "role_id"),
                    @JoinColumn(name = "role_name", referencedColumnName = "role")
            })
    private List<Role> roles;

    @OneToMany(fetch=FetchType.LAZY,
            mappedBy = "userId",
            targetEntity=Experiment.class)
    private List<Experiment> listExperiments;
    /*@JoinTable(
            name = "users_experiments",
            joinColumns ={
                    @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
                    @JoinColumn(name = "username", referencedColumnName = "username"),
                    @JoinColumn(name = "email", referencedColumnName = "email")
            },
            inverseJoinColumns ={
                    @JoinColumn(name = "experiment_id", referencedColumnName = "experiment_id"),
                    @JoinColumn(name = "experiment_name", referencedColumnName = "experiment_name")
            })*/


    public User(){
        this.listExperiments = new ArrayList<>();
    }

    public User(Long id) {
        this.id = id;
        this.listExperiments = new ArrayList<>();
    }

    public User(Long id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(String username, String password, String email, List<Role> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;

        this.listExperiments = new ArrayList<>();
    }

    public User(String username, String password, String firstName, String lastname, String email, String addressDirection, Integer phone) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastname;
        this.email = email;
        this.addressDirection = addressDirection;
        this.phone = phone;
        this.listExperiments = new ArrayList<>();
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getId() { return id; }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public void setLastName(String lastname) {
        this.lastName = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddressDirection() {
        return addressDirection;
    }

    public void setAddressDirection(String addressDirection) {
        this.addressDirection = addressDirection;
    }

    public Integer getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {
        this.phone = phone;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role){
        if(!this.roles.contains(role)){
            this.roles.add(role);
        }

        /*if(!role.getUsers().contains(this)){
            role.getUsers().add(this);
        }*/
    }

    public void removeRole(Role role){
        this.roles.remove(role);
        //role.getUsers().remove(this);
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Experiment addExperiment(Experiment experiment) {
        this.listExperiments.add(experiment);
        experiment.setUserId(this);
        return experiment;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + "*********" + '\'' +
                ", roles=" + roles +
                '}';
    }
}