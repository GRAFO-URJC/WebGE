package com.gramevapp.web.controller;

import com.gramevapp.web.model.User;
import com.gramevapp.web.model.UserRegistrationDto;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class AdminController {
    @Autowired
    private UserService userService;

    @GetMapping("/admin")
    public String adminPage() {
        return "admin/adminPage";
    }

    @GetMapping("/admin/registrationPage")
    public String showRegistrationForm(Model model,
                                       @ModelAttribute("user") UserRegistrationDto userDto) {
        model.addAttribute("user",userDto);

        return "admin/registrationPage";
    }

    @PostMapping("/admin/registrationPage")
    public String registerUserAccount(  Model model,
                                        @ModelAttribute("user") @Valid UserRegistrationDto userDto,
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
            return "admin/registrationPage";
        }

        userService.saveUser(userDto);

        model.addAttribute("message", "User registered successfully");

        return "redirect:/admin";
    }

}
