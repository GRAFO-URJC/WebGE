package com.gramevapp.web.controller;

import com.gramevapp.web.model.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class AdminController extends UserCommon {

    private static final String MESSAGE = "message";

    @GetMapping("/admin")
    public String adminPage(Model model,
                            @RequestParam(value = "messageUserCreated", required = false) String message) {
        User user = userService.getLoggedInUser();
        if (message != null) {
            model.addAttribute("messageUserCreated", message);
        }
        if (user.getPassword().equals("$2a$11$hwnvHO4u./7PBsClAXe1fuPIat1sqitn7EYvti9ajWpONIqx7pYB2")) {
            model.addAttribute(MESSAGE, "Please change the password, now is the default password, you can click" +
                    " the message.");
        }
        HashMap<User, Run> summaryExperiment = new HashMap<>();
        List<Experiment> experimentList = experimentService.findAll();
        for (Experiment experiment : experimentList) {
            User u = userService.getById(experiment.getUserId());
            if (experiment.getIdRunList() != null && !experiment.getIdRunList().isEmpty()) {
                summaryExperiment.put(u, experiment.getIdRunList().get(experiment.getIdRunList().size() - 1));
            }
        }
        model.addAttribute("summaryExperiment", summaryExperiment);
        model.addAttribute("userList", new ArrayList<>(summaryExperiment.keySet()));
        model.addAttribute("running", Run.Status.RUNNING);
        return "admin/adminPage";
    }

    private Run compareRun(Run run1, Run run2) {
        // if runnning use ini date
        if (run1.getStatus().equals(Run.Status.RUNNING) && run2.getStatus().equals(Run.Status.RUNNING)) {
            return run1.getIniDate().compareTo(run2.getIniDate()) >= 0 ? run1 : run2;
        } else if (run1.getStatus().equals(Run.Status.RUNNING) && !run2.getStatus().equals(Run.Status.RUNNING)) {
            return run1.getIniDate().compareTo(run2.getModificationDate()) >= 0 ? run1 : run2;
        } else if (!run1.getStatus().equals(Run.Status.RUNNING) && run2.getStatus().equals(Run.Status.RUNNING)) {
            return run1.getModificationDate().compareTo(run2.getIniDate()) >= 0 ? run1 : run2;
        }
        return run1.getModificationDate().compareTo(run2.getModificationDate()) >= 0 ? run1 : run2;

    }

    @GetMapping("/admin/registrationPage")
    public String showRegistrationForm(Model model,
                                       @ModelAttribute("user") UserRegistrationDto userDto) {
        model.addAttribute("user", userDto);
        return "admin/registrationPage";
    }

    @PostMapping("/admin/registrationPage")
    public String registerUserAccount(Model model,
                                      @ModelAttribute("user") @Valid UserRegistrationDto userDto,
                                      BindingResult result,
                                      RedirectAttributes redirectAttrs) {

        User existingEmail = userService.findByEmail(userDto.getEmail());
        User existingUsername = userService.findByUsername(userDto.getUsername());

        if (existingEmail != null) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        if (existingUsername != null) {
            result.rejectValue("username", null, "There is already an account registered with that username");
        }

        if (result.hasErrors()) {
            return "admin/registrationPage";
        }

        userService.saveUser(userDto);

        redirectAttrs.addAttribute("messageUserCreated", "User created");
        model.addAttribute(MESSAGE, "User registered successfully");

        return "redirect:/admin";
    }

    @GetMapping("/admin/profile")
    public String adminProfile(Model model,
                               @RequestParam(value = "message", required = false) String message,
                               @RequestParam(value = "areaActive", required = false) String areaActive) {
        User user = userService.getLoggedInUser();

        if (user.getUserDetails() == null) {
            UserDetails userDetails = new UserDetails();
            userDetails.setUser(user);
            user.setUserDetails(userDetails);
            userService.updateUser();
        }

        UserUpdateBasicInfoDto upBasicDto = new UserUpdateBasicInfoDto();
        UserUpdatePasswordDto upPassDto = new UserUpdatePasswordDto();
        UserUpdateAboutDto updAboutDto = new UserUpdateAboutDto();
        UserUpdateStudyDto upStudyDto = new UserUpdateStudyDto();

        if (message != null) {
            model.addAttribute(MESSAGE, message);
        }

        if (areaActive != null)
            model.addAttribute("areaActive", areaActive);
        else
            model.addAttribute("areaActive", "basicActive");

        model.addAttribute("userLogged", user);
        model.addAttribute("userBasicInfo", upBasicDto);
        model.addAttribute("userPassword", upPassDto);
        model.addAttribute("userStudy", upStudyDto);
        model.addAttribute("userAboutMe", updAboutDto);

        return "user/profile";
    }

    @PostMapping(value = "/admin/updateAdminPassword")
    public String updateAdminPassword(Model model,
                                      @ModelAttribute("userPassword") @Valid UserUpdatePasswordDto userUpDto,
                                      BindingResult result,
                                      RedirectAttributes redirectAttrs) {
        return updatePassword(model, userUpDto, result, redirectAttrs, "/admin/profile");
    }

    @PostMapping(value = "/admin/updateAdminBasicInfo")
    public String updateAdminInformation(Model model,
                                         @ModelAttribute("userBasicInfo") @Valid UserUpdateBasicInfoDto userUpDto,
                                         BindingResult result,
                                         RedirectAttributes redirectAttrs) {
        return updateBasicInfo(model, userUpDto, result, redirectAttrs, "/admin/profile");
    }

    @GetMapping("/admin/userList")
    public String adminUserList(Model model) {
        model.addAttribute("userList", userService.findAllUserWithoutAdmin());
        return "admin/userList";
    }

    @PostMapping(value = "/admin/deleteUser", params = "deleteUser")
    public
    @ResponseBody
    Long deleteUser(@RequestParam("userId") String userId) {
        Long id = Long.parseLong(userId);

        try {
            userService.deleteUserById(id);
        } catch (DataIntegrityViolationException e) {
            System.out.println(e.getCause() instanceof org.hibernate.exception.ConstraintViolationException);
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                return (long) -1;
            }
        }

        return id;
    }

    @PostMapping(value = "/admin/changePassword")
    @ResponseBody
    public void changePassword(@RequestParam("userId") String userId, @RequestParam("password") String password) {
        User user = userService.getById(Long.parseLong(userId));
        user.setPassword(passwordEncoder.encode(password));
        userService.save(user);
    }
}
