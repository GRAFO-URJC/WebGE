package com.gramevapp.web.controller;

import com.gramevapp.web.model.*;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UploadFileService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.imgscalr.Scalr;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Controller
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    UploadFileService uploadFileService;

    @Autowired
    RunService runService;

    private final String PROFILE_PICTURE_PATH = "." + File.separator + "resources" + File.separator + "files" + File.separator + "profilePicture" + File.separator + "";

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/user/profile")
    public String userProfile(Model model){
        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        UserUpdateBasicInfoDto upBasicInfoDto = new UserUpdateBasicInfoDto();
        UserUpdateAboutDto upAboutDto = new UserUpdateAboutDto();
        UserUpdatePasswordDto upPassDto = new UserUpdatePasswordDto();
        UserUpdateStudyDto upStudy = new UserUpdateStudyDto();

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
        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }
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
        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }
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

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }

        user.setFirstName(userUpDto.getFirstName());
        user.setLastName(userUpDto.getLastName());
        user.setPhone(userUpDto.getPhone());
        user.setAddressDirection(userUpDto.getAddressDirection());
        user.setState(userUpDto.getState());
        user.setCity(userUpDto.getCity());
        user.setZipcode(userUpDto.getZipcode());

        if(userUpDto != null) {
            // Profile photo update
            Format formatter = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
            String fileName = formatter.format(Calendar.getInstance().getTime()) + "_thumbnail.jpg";

            MultipartFile pictureFile = userUpDto.getProfilePicture();
            if (!pictureFile.isEmpty()) {
                try {
                    File dir = new File(PROFILE_PICTURE_PATH + user.getId());   // Create the directory to save datatype files
                    if (!dir.exists())
                        dir.mkdirs();

                    byte[] bytes = pictureFile.getBytes();

                    ByteArrayInputStream imageInputStream = new ByteArrayInputStream(bytes);
                    BufferedImage image = ImageIO.read(imageInputStream);
                    BufferedImage thumbnail = Scalr.resize(image, 200);

                    File thumbnailOut = new File(dir.getAbsolutePath() + File.separator + fileName);
                    ImageIO.write(thumbnail, "png", thumbnailOut);

                    UploadFile uploadFile = new UploadFile();
                    uploadFile.setFilePath(fileName);

                    user.setProfilePicture(uploadFile);
                    // userService.updateProfilePicture(user, fileName);

                    /*File dirToClean = new File(PROFILE_PICTURE_PATH + user.getId());
                    FileUtils.cleanDirectory(dirToClean);*/

                    System.out.println("Image Saved::: " + fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // END - Profile photo update
        }

        if(userService.findByEmail(userUpDto.getEmail())==null)
            user.setEmail(userUpDto.getEmail());

        userService.save(user);

        // model.addAttribute("image", user.getUploadFile().getBData());
        model.addAttribute("userLogged", user);     // If we don't set the model. In ${userLogged.getUsername()}" we will have fail
        model.addAttribute("message", "Basic user information updated");
        return "redirect:/user/profile";
    }

    @RequestMapping(value="/user/updateAboutMe", method= RequestMethod.POST)
    public String updateAboutMe(Model model,
                                @ModelAttribute("userAboutMe") @Valid UserUpdateAboutDto userUpDto,
                                BindingResult result){

        User user = userService.getLoggedInUser();
        if(user == null){
            System.out.println("User not authenticated");
            return "redirect:/login";
        }
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
    }

    @RequestMapping(value="/user/profile_picture", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] profilePicture(Model model) throws IOException {
        User user = userService.getLoggedInUser();

        File dir = new File(PROFILE_PICTURE_PATH + user.getId());
        if (!dir.exists())
            dir.mkdirs();

        String profilePicture = PROFILE_PICTURE_PATH + user.getId() + File.separator + user.getProfilePicture().getFilePath();

        if(new File(profilePicture).exists()) {
            return IOUtils.toByteArray(new FileInputStream(profilePicture));
        } else {
            return null;
        }
    }

    /*@RequestMapping(value="/user/profile-picture", method = RequestMethod.GET)
    public String profilePicture(Model model) {
        User user = userService.getLoggedInUser();

        model.addAttribute("userLogged", user);

        return "redirect:user/profile";
    }*/

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