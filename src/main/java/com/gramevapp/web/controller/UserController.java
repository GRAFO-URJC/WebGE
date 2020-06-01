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

@Controller
public class UserController extends UserCommon {

    @GetMapping("/user/profile")
    public String userProfile(Model model,
                              @RequestParam(value = "message", required = false) String message,
                              @RequestParam(value = "areaActive", required = false) String areaActive) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            System.out.println("User not authenticated");
            return "redirect:/login";
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

    @RequestMapping(value = "/user/updateUserPassword", method = RequestMethod.POST)
    public String updateUserPassword(Model model,
                                     @ModelAttribute("userPassword") @Valid UserUpdatePasswordDto userUpDto,
                                     BindingResult result,
                                     RedirectAttributes redirectAttrs) {
        return updatePassword(model, userUpDto, result, redirectAttrs, "/user/profile");
    }

    @RequestMapping(value = "/user/updateStudy", method = RequestMethod.POST)
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
            model.addAttribute("userPassword", upPassDto);
            model.addAttribute("userBasicInfo", upBasicDto);
            model.addAttribute("userLogged", user);
            return "user/profile";
        }

        user.getUserDetails().setStudyInformation(userUpDto.getStudyInformation());
        user.getUserDetails().setWorkInformation(userUpDto.getWorkInformation());

        userService.save(user);
        redirectAttrs.addAttribute("message", "Study/Work area information updated").addFlashAttribute("Study/Work", "Study/Work area");
        redirectAttrs.addAttribute("areaActive", "studyActive").addFlashAttribute("studyActive", "Activate Study/Work area");
        return "redirect:/user/profile";
    }

    @RequestMapping(value = "/user/updateUserBasicInfo", method = RequestMethod.POST)
    public String updateUserInformation(Model model,
                                        @ModelAttribute("userBasicInfo") @Valid UserUpdateBasicInfoDto userUpDto,
                                        BindingResult result,
                                        RedirectAttributes redirectAttrs) {

        return updateBasicInfo(model, userUpDto, result, redirectAttrs, "/user/profile");
    }

    @RequestMapping(value = "/user/updateAboutMe", method = RequestMethod.POST)
    public String updateAboutMe(Model model,
                                @ModelAttribute("userAboutMe") @Valid UserUpdateAboutDto userUpDto,
                                BindingResult result,
                                RedirectAttributes redirectAttrs) {

        User user = userService.getLoggedInUser();
        if (user == null) {
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            UserUpdateStudyDto upStudyDto = new UserUpdateStudyDto();
            upStudyDto.setWorkInformation(user.getUserDetails().getWorkInformation());
            upStudyDto.setStudyInformation(user.getUserDetails().getStudyInformation());

            UserUpdatePasswordDto upPassDto = new UserUpdatePasswordDto();
            upPassDto.setPassword("");

            UserUpdateBasicInfoDto upBasicDto = userSet(user);

            model.addAttribute("userPassword", upPassDto);
            model.addAttribute("userStudy", upStudyDto);
            model.addAttribute("userBasicInfo", upBasicDto);
            model.addAttribute("userLogged", user);
            return "redirect:/user/profile";
        }
        user.getUserDetails().setAboutMe(userUpDto.getAboutMe());
        userService.save(user);

        redirectAttrs.addAttribute("message", "About me information area updated").addFlashAttribute("aboutMe", "About me area");
        redirectAttrs.addAttribute("areaActive", "aboutMeActive").addFlashAttribute("aboutMeActive", "Activate About me area");
        return "redirect:/user/profile";
    }

    @GetMapping("/user")
    public String userIndex(HttpServletRequest request) {
        UserDetails userDetails = userService.getLoggedInUser().getUserDetails();
        request.getSession().setAttribute("userDetails", userDetails);
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
        return "/error/access-denied";
    }

    @ModelAttribute("user") // Without this. The registration won't work
    public UserRegistrationDto userRegistrationDto() {
        return new UserRegistrationDto();
    }

    @RequestMapping(value = "/user/profile_picture", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
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
            return null;
        }
    }
}