package com.gramevapp.web.controller;

import com.gramevapp.web.model.*;
import com.gramevapp.web.service.ExperimentService;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import net.sourceforge.jeval.function.math.Exp;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class AdminController extends UserCommon {
    @GetMapping("/admin")
    public String adminPage(Model model,
                            @RequestParam(value = "messageUserCreated", required = false) String message) {
        User user = userService.getLoggedInUser();
        if (message != null) {
            model.addAttribute("messageUserCreated", message);
        }
        if (user.getPassword().equals("$2a$11$hwnvHO4u./7PBsClAXe1fuPIat1sqitn7EYvti9ajWpONIqx7pYB2")) {
            model.addAttribute("message", "Please change the password, now is the default password, you can click" +
                    " the message.");
        }
        HashMap<User, Run> summaryExperiment = new HashMap<>();
        List<Experiment> experimentList = experimentService.findAll();
        for (Experiment experiment : experimentList) {
            User u = experiment.getUserId();
            Run newRunCompare = runService.findByRunId(experiment.getDefaultRunId());
            Run run = summaryExperiment.get(u);
            if (run == null) {
                summaryExperiment.put(u, newRunCompare);
            } else {
                summaryExperiment.put(u, compareRun(run, newRunCompare));
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
        model.addAttribute("message", "User registered successfully");

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
            model.addAttribute("message", message);
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

    @RequestMapping(value = "/admin/updateAdminPassword", method = RequestMethod.POST)
    public String updateAdminPassword(Model model,
                                      @ModelAttribute("userPassword") @Valid UserUpdatePasswordDto userUpDto,
                                      BindingResult result,
                                      RedirectAttributes redirectAttrs) {
        return updatePassword(model, userUpDto, result, redirectAttrs, "/admin/profile");
    }

    @RequestMapping(value = "/admin/updateAdminBasicInfo", method = RequestMethod.POST)
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

    @RequestMapping(value = "/admin/deleteUser", method = RequestMethod.POST, params = "deleteUser")
    public
    @ResponseBody
    Long deleteUser(@RequestParam("userId") String userId) {
        Long id = Long.parseLong(userId);

        try {
            userService.deleteUserById(id);
        } catch (DataIntegrityViolationException e) {
            System.out.println(e.getCause() instanceof org.hibernate.exception.ConstraintViolationException);
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                return Long.valueOf(-1);
            }
        }

        return id;
    }

    @RequestMapping(value = "/admin/changePassword", method = RequestMethod.POST)
    @ResponseBody
    public void changePassword(@RequestParam("userId") String userId, @RequestParam("password") String password) {
        User user = userService.getById(Long.parseLong(userId));
        user.setPassword(passwordEncoder.encode(password));
        userService.save(user);
    }
}
