package com.gramevapp.web.controller;

import com.gramevapp.web.model.*;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class AdminController {
    @Autowired
    private UserService userService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/admin")
    public String adminPage(Model model,
                            @RequestParam(value="messageUserCreated", required=false) String message) {
        User user = userService.getLoggedInUser();
        if(message!=null){
            model.addAttribute("messageUserCreated",message);
        }
        if(user.getPassword().equals("$2a$11$hwnvHO4u./7PBsClAXe1fuPIat1sqitn7EYvti9ajWpONIqx7pYB2")){
            model.addAttribute("message","Please change the password, now is the default password.");
        }
        return "admin/adminPage";
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

        redirectAttrs.addAttribute("messageUserCreated","User created");
        model.addAttribute("message", "User registered successfully");

        return "redirect:/admin";
    }

    @GetMapping("/admin/profile")
    public String adminProfile(Model model,
                               @RequestParam(value = "message", required = false) String message,
                               @RequestParam(value = "areaActive", required = false) String areaActive) {
        User user = userService.getLoggedInUser();

        if (user.getUserDetails() == null) {
            UserDetails userDetails=new UserDetails();
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

        return "admin/profile";
    }

    @RequestMapping(value = "/admin/updateAdminPassword", method = RequestMethod.POST)
    public String updateAdminPassword(Model model,
                                      @ModelAttribute("userPassword") @Valid UserUpdatePasswordDto userUpDto,
                                      BindingResult result,
                                      RedirectAttributes redirectAttrs) {
        User user = userService.getLoggedInUser();

        if (result.hasErrors()) {
            UserUpdateAboutDto updAboutDto = new UserUpdateAboutDto();
            updAboutDto.setAboutMe(user.getUserDetails().getAboutMe());

            UserUpdateStudyDto upStudyDto = new UserUpdateStudyDto();
            upStudyDto.setWorkInformation(user.getUserDetails().getWorkInformation());
            upStudyDto.setStudyInformation(user.getUserDetails().getStudyInformation());

            UserUpdateBasicInfoDto upBasicDto = new UserUpdateBasicInfoDto();
            upBasicDto.setFirstName(user.getUserDetails().getFirstName());
            upBasicDto.setLastName(user.getUserDetails().getLastName());
            upBasicDto.setEmail(user.getEmail());
            upBasicDto.setPhone(user.getUserDetails().getPhone());
            upBasicDto.setAddressDirection(user.getUserDetails().getAddressDirection());
            upBasicDto.setCity(user.getUserDetails().getCity());
            upBasicDto.setState(user.getUserDetails().getState());
            upBasicDto.setZipcode(user.getUserDetails().getZipcode());

            model.addAttribute("userAboutMe", updAboutDto);
            model.addAttribute("userStudy", upStudyDto);
            model.addAttribute("userBasicInfo", upBasicDto);
            model.addAttribute("userLogged", user);
            model.addAttribute("areaActive", "basicActive");
            return "admin/profile";
        }
        user.setPassword(passwordEncoder.encode(userUpDto.getPassword()));
        userService.save(user);

        redirectAttrs.addAttribute("message", "Password saved").addFlashAttribute("password", "Password info area");
        redirectAttrs.addAttribute("areaActive", "passwordActive").addFlashAttribute("passwordActive", "Activate password area");
        return "redirect:/admin/profile";
    }

}
