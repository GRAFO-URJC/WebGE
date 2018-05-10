package com.gramevapp.web.controller;

import com.gramevapp.web.model.*;
import com.gramevapp.web.service.UploadFileService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Base64;

@Controller
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    UploadFileService uploadFileService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/user/profile")
    public String userProfile(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {    // User not authenticated
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        User user = userService.findByUsername(authentication.getName());

        UserUpdateBasicInfoDto upBasicInfoDto = new UserUpdateBasicInfoDto();
        UserUpdateAboutDto upAboutDto = new UserUpdateAboutDto();
        UserUpdatePasswordDto upPassDto = new UserUpdatePasswordDto();
        UserUpdateStudyDto upStudy = new UserUpdateStudyDto();

        if(user.getUploadFile() == null)
            user.setUploadFile(new UploadFile());

        model.addAttribute("image", user.getUploadFile().getBData());
        model.addAttribute("userLogged", user);
        model.addAttribute("userBasicInfo", upBasicInfoDto);
        model.addAttribute("userPassword", upPassDto);
        model.addAttribute("userStudy", upStudy);
        model.addAttribute("userAboutMe", upAboutDto);

        return "user/profile";
    }

    @RequestMapping(value="/user/updateUserPassword", method=RequestMethod.POST)
    public String updateUserPassword(Model model,
                                     @ModelAttribute("userPassword") @Valid UserUpdatePasswordDto userUpDto,
                                     BindingResult result){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {    // User not authenticated
            System.out.println("User not authenticated");
            return "redirect:/login";
        }
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("userLogged", user);     // If we don't set the model. In ${userLogged.getUsername()}" we will have fail

        if(result.hasErrors()){
            return "/user/profile";
        }

        if(userUpDto.getPassword().equals(userUpDto.getConfirmPassword())) {
            user.setPassword(passwordEncoder.encode(userUpDto.getPassword()));
            userService.save(user);
        }


        return "redirect:/user/profile";
    }

    @RequestMapping(value="/user/updateStudy", method=RequestMethod.POST)
    public String updateUserStudy(Model model,
                                  @ModelAttribute("userStudy") @Valid UserUpdateStudyDto userUpDto,
                                  BindingResult result) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {    // User not authenticated
            System.out.println("User not authenticated");
            return "redirect:/login";
        }
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("userLogged", user);     // If we don't set the model. In ${userLogged.getUsername()}" we will have fail

        if(result.hasErrors())
            return "/user/profile";

        user.setStudyInformation(userUpDto.getStudyInformation());
        user.setWorkInformation(userUpDto.getWorkInformation());

        userService.save(user);

        model.addAttribute("message", "Study/Work user information updated");

        return "redirect:/user/profile";
    }

    @RequestMapping(value="/user/updateUserBasicInfo",method=RequestMethod.POST)
    public String updateUserInformation(Model model,
                                        @ModelAttribute("userBasicInfo") @Valid UserUpdateBasicInfoDto userUpDto,
                                        BindingResult result){

        if(result.hasErrors()){
            return "/user/profile";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {    // User not authenticated
            System.out.println("User not authenticated");
            return "redirect:/login";
        }
        User user = userService.findByUsername(authentication.getName());

        user.setFirstName(userUpDto.getFirstName());
        user.setLastName(userUpDto.getLastName());
        user.setPhone(userUpDto.getPhone());
        user.setAddressDirection(userUpDto.getAddressDirection());
        user.setState(userUpDto.getState());
        user.setCity(userUpDto.getCity());
        user.setZipcode(userUpDto.getZipcode());

        UploadFile upUserPhoto = new UploadFile();
        byte[] data = userUpDto.getUploadFile().getData();

        String bData = Base64.getEncoder().encodeToString(data);

        upUserPhoto.setData(data);
        upUserPhoto.setBData(bData);

        user.setUploadFile(upUserPhoto);

        uploadFileService.saveUploadFile(userUpDto.getUploadFile());

        if(userService.findByEmail(userUpDto.getEmail())==null)
            user.setEmail(userUpDto.getEmail());

        userService.save(user);

        model.addAttribute("image", user.getUploadFile().getBData());
        model.addAttribute("userLogged", user);     // If we don't set the model. In ${userLogged.getUsername()}" we will have fail
        model.addAttribute("message", "Basic user information updated");
        return "/user/profile";
    }

    @RequestMapping(value="/user/updateAboutMe", method= RequestMethod.POST)
    public String updateAboutMe(Model model,
                                @ModelAttribute("userAboutMe") @Valid UserUpdateAboutDto userUpDto,
                                BindingResult result){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("userLogged", user);     // If we don't set the model. In ${userLogged.getUsername()}" we will have fail

        if(result.hasErrors()){
            return "/user/profile";
        }
        user.setAboutMe(userUpDto.getAboutMe());

        userService.save(user);

        model.addAttribute("userLogged", user);
        model.addAttribute("message", "About me user information updated");
        return "/user/profile";
    }

    @GetMapping("/user")
    public String userIndex() {
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

    @GetMapping("/registration")
    public String showRegistrationForm() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // https://stackoverflow.com/questions/26101738/why-is-the-anonymoususer-authenticated-in-spring-security
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/user";
        }

        return "userRegistration";
    }

    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserRegistrationDto userDto,
                                      BindingResult result){

        User existingEmail = userService.findByEmail(userDto.getEmail());
        User existingUsername = userService.findByUsername(userDto.getUsername());

        if(existingEmail != null){
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        if(existingUsername != null){
            result.rejectValue("username", null, "There is already an account registered with that username");
        }

        if (result.hasErrors()){
            return "userRegistration";
        }

        userService.save(userDto);
        return "login";
        //return "redirect:/userRegistration?success";
    }

    /**
     *
     return "redirect:/books";

     It returns to the client (browser) which interprets the http response and automatically calls the redirect URL

     return "jsp/books/booksList";

     It process the JSP and send the HTML to the client

     return "forward:/books";

     It transfer the request and calls the URL direct in the server side.
     *
     */
}