package com.gramevapp.web.controller;

import com.gramevapp.web.model.*;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

@Controller
public class UserController extends UserCommon {

    private static final String MESSAGE = "message";
    private static final String AREAACTIVE = "areaActive";
    private static final String USERLOGGED = "userLogged";
    private static final String USERBASICINFO = "userBasicInfo";
    private static final String USERPASSWORD = "userPassword";
    private static final String REDIRECTPROFILE = "redirect:/user/profile";
    @GetMapping("/user/profile")
    public String userProfile(Model model,
                              @RequestParam(value = MESSAGE, required = false) String message,
                              @RequestParam(value = AREAACTIVE, required = false) String areaActive) {
        User user = userService.getLoggedInUser();
        UserUpdateBasicInfoDto upBasicDto = new UserUpdateBasicInfoDto();
        UserUpdatePasswordDto upPassDto = new UserUpdatePasswordDto();
        UserUpdateAboutDto updAboutDto = new UserUpdateAboutDto();
        UserUpdateStudyDto upStudyDto = new UserUpdateStudyDto();

        if (message != null) {
            model.addAttribute(MESSAGE, message);
        }

        if (areaActive != null)
            model.addAttribute(AREAACTIVE, areaActive);
        else
            model.addAttribute(AREAACTIVE,BASICACTIVE);

        model.addAttribute(USERLOGGED, user);
        model.addAttribute(USERBASICINFO, upBasicDto);
        model.addAttribute(USERPASSWORD, upPassDto);
        model.addAttribute("userStudy", upStudyDto);
        model.addAttribute("userAboutMe", updAboutDto);

        return "user/profile";
    }

    @PostMapping(value = "/user/updateUserPassword")
    public String updateUserPassword(Model model,
                                     @ModelAttribute(USERPASSWORD) @Valid UserUpdatePasswordDto userUpDto,
                                     BindingResult result,
                                     RedirectAttributes redirectAttrs) {
        return updatePassword(model, userUpDto, result, redirectAttrs, "/user/profile");
    }

    @PostMapping(value = "/user/updateStudy")
    public String updateUserStudy(Model model,
                                  @ModelAttribute("userStudy") @Valid UserUpdateStudyDto userUpDto,
                                  BindingResult result,
                                  RedirectAttributes redirectAttrs) {
        User user = userService.getLoggedInUser();

        if (result.hasErrors()) {
            UserUpdateAboutDto updAboutDto = new UserUpdateAboutDto();
            updAboutDto.setAboutMe(user.getUserDetails().getAboutMe());

            UserUpdateBasicInfoDto upBasicDto = userSet(user);

            UserUpdatePasswordDto upPassDto = new UserUpdatePasswordDto();
            upPassDto.setPassword("");

            model.addAttribute("userAboutMe", updAboutDto);
            model.addAttribute(USERPASSWORD, upPassDto);
            model.addAttribute(USERBASICINFO, upBasicDto);
            model.addAttribute(USERLOGGED, user);
            return "user/profile";
        }

        user.getUserDetails().setStudyInformation(userUpDto.getStudyInformation());
        user.getUserDetails().setWorkInformation(userUpDto.getWorkInformation());

        userService.save(user);
        redirectAttrs.addAttribute(MESSAGE, "Study/Work area information updated").addFlashAttribute("Study/Work", "Study/Work area");
        redirectAttrs.addAttribute(AREAACTIVE, "studyActive").addFlashAttribute("studyActive", "Activate Study/Work area");
        return REDIRECTPROFILE;
    }

    @PostMapping(value = "/user/updateUserBasicInfo")
    public String updateUserInformation(Model model,
                                        @ModelAttribute(USERBASICINFO) @Valid UserUpdateBasicInfoDto userUpDto,
                                        BindingResult result,
                                        RedirectAttributes redirectAttrs) {

        return updateBasicInfo(model, userUpDto, result, redirectAttrs, "/user/profile");
    }

    @PostMapping(value = "/user/updateAboutMe")
    public String updateAboutMe(Model model,
                                @ModelAttribute("userAboutMe") @Valid UserUpdateAboutDto userUpDto,
                                BindingResult result,
                                RedirectAttributes redirectAttrs) {

        User user = userService.getLoggedInUser();

        if (result.hasErrors()) {
            UserUpdateStudyDto upStudyDto = new UserUpdateStudyDto();
            upStudyDto.setWorkInformation(user.getUserDetails().getWorkInformation());
            upStudyDto.setStudyInformation(user.getUserDetails().getStudyInformation());

            UserUpdatePasswordDto upPassDto = new UserUpdatePasswordDto();
            upPassDto.setPassword("");

            UserUpdateBasicInfoDto upBasicDto = userSet(user);

            model.addAttribute(USERPASSWORD, upPassDto);
            model.addAttribute("userStudy", upStudyDto);
            model.addAttribute(USERBASICINFO, upBasicDto);
            model.addAttribute(USERLOGGED, user);
        } else {
            user.getUserDetails().setAboutMe(userUpDto.getAboutMe());
            userService.save(user);

            redirectAttrs.addAttribute(MESSAGE, "About me information area updated").addFlashAttribute("aboutMe", "About me area");
            redirectAttrs.addAttribute(AREAACTIVE, "aboutMeActive").addFlashAttribute("aboutMeActive", "Activate About me area");
        }
        return REDIRECTPROFILE;
    }

    @GetMapping("/user")
    public String userIndex(HttpServletRequest request) {
        if (userService.getLoggedInUser() != null) {
            UserDetails userDetails = userService.getLoggedInUser().getUserDetails();
            request.getSession().setAttribute("userDetails", userDetails);
        }
        return "user/index";
    }

    @GetMapping("/login")
    public String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // https://stackoverflow.com/questions/26101738/why-is-the-anonymoususer-authenticated-in-spring-security
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/user";
        }

        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @ModelAttribute("user") // Without this. The registration won't work
    public UserRegistrationDto userRegistrationDto() {
        return new UserRegistrationDto();
    }

    @GetMapping(value = "/user/profile_picture", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody
    byte[] profilePicture() throws IOException {
        User user = userService.getLoggedInUser();

        File dir = new File(PROFILE_PICTURE_PATH + user.getId());
        if (!dir.exists())
            dir.mkdirs();

        String profilePicture = PROFILE_PICTURE_PATH + user.getId() + File.separator + user.getUserDetails().getProfilePicture().getFilePath();

        if (new File(profilePicture).exists()) {
            return IOUtils.toByteArray(new FileInputStream(profilePicture));
        } else {
            return new byte[1];
        }
    }
}