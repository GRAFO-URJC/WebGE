package com.gramevapp.web.controller;

import com.gramevapp.web.model.*;
import com.gramevapp.web.service.ExperimentService;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.UserService;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UserCommon {

    @Autowired
    protected RunService runService;

    @Autowired
    protected ExperimentService experimentService;

    @Autowired
    protected UserService userService;

    protected static final String PROFILE_PICTURE_PATH = "." + File.separator + "resources" + File.separator + "files" + File.separator + "profilePicture" + File.separator + "";
    protected static final String AREAACTIVE = "areaActive";
    protected static final String PASSWORDACTIVE = "passwordActive";
    protected static final String BASICACTIVE = "basicActive";

    protected BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    protected String updatePassword(Model model,
                                    UserUpdatePasswordDto userUpDto,
                                    BindingResult result,
                                    RedirectAttributes redirectAttrs,
                                    String link) {
        User user = userService.getLoggedInUser();
        boolean checkPassword = passwordEncoder.matches(userUpDto.getOldPassword(), user.getPassword());

        if (result.hasErrors() || !checkPassword) {
            UserUpdateAboutDto updAboutDto = new UserUpdateAboutDto();
            updAboutDto.setAboutMe(user.getUserDetails().getAboutMe());

            UserUpdateStudyDto upStudyDto = new UserUpdateStudyDto();
            upStudyDto.setWorkInformation(user.getUserDetails().getWorkInformation());
            upStudyDto.setStudyInformation(user.getUserDetails().getStudyInformation());

            UserUpdateBasicInfoDto upBasicDto = userSet(user);

            model.addAttribute("userAboutMe", updAboutDto);
            model.addAttribute("userStudy", upStudyDto);
            model.addAttribute("userBasicInfo", upBasicDto);
            model.addAttribute("userLogged", user);
            model.addAttribute(AREAACTIVE, PASSWORDACTIVE);
            model.addAttribute("oldPasswordCheck", !checkPassword);
            return "user/profile";
        }
        user.setPassword(passwordEncoder.encode(userUpDto.getPassword()));
        userService.save(user);

        redirectAttrs.addAttribute("message", "Password saved").addFlashAttribute("password", "Password info area");
        redirectAttrs.addAttribute(AREAACTIVE, PASSWORDACTIVE).addFlashAttribute(PASSWORDACTIVE, "Activate password area");
        return "redirect:" + link;
    }

    protected String updateBasicInfo(Model model,
                                     UserUpdateBasicInfoDto userUpDto,
                                     BindingResult result,
                                     RedirectAttributes redirectAttrs,
                                     String link) {

        User user = userService.getLoggedInUser();

        if (result.hasErrors()) {
            user.getUserDetails().setProfilePicture(null);

            UserUpdateAboutDto upAboutDto = new UserUpdateAboutDto();
            upAboutDto.setAboutMe(user.getUserDetails().getAboutMe());

            UserUpdatePasswordDto upPassDto = new UserUpdatePasswordDto();
            upPassDto.setPassword("");

            UserUpdateStudyDto upStudyDto = new UserUpdateStudyDto();
            upStudyDto.setWorkInformation(user.getUserDetails().getWorkInformation());
            upStudyDto.setStudyInformation(user.getUserDetails().getStudyInformation());

            model.addAttribute("userAboutMe", upAboutDto);
            model.addAttribute("userPassword", upPassDto);
            model.addAttribute("userStudy", upStudyDto);
            model.addAttribute("userLogged", user);
            model.addAttribute(AREAACTIVE, BASICACTIVE);
            return "user/profile";
        }

        user.getUserDetails().setFirstName(userUpDto.getFirstName());
        user.getUserDetails().setLastName(userUpDto.getLastName());
        user.getUserDetails().setPhone(userUpDto.getPhone());
        user.getUserDetails().setAddressDirection(userUpDto.getAddressDirection());
        user.getUserDetails().setState(userUpDto.getState());
        user.getUserDetails().setCity(userUpDto.getCity());
        user.getUserDetails().setZipcode(userUpDto.getZipcode());
        user.setEmail(userUpDto.getEmail());
        user.setInstitution(userUpDto.getInstitution());

        if (!userUpDto.getProfilePicture().isEmpty()) {
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

                    user.getUserDetails().setProfilePicture(uploadFile);
                    System.out.println("Image Saved::: " + fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        userService.save(user);

        redirectAttrs.addAttribute("message", "Basic information updated").addFlashAttribute("aboutMe", "Basic information area");
        redirectAttrs.addAttribute(AREAACTIVE, BASICACTIVE).addFlashAttribute(BASICACTIVE, "Basic information area");
        return "redirect:" + link;
    }

    protected UserUpdateBasicInfoDto userSet(User user) {
        UserUpdateBasicInfoDto upBasicDto = new UserUpdateBasicInfoDto();
        upBasicDto.setFirstName(user.getUserDetails().getFirstName());
        upBasicDto.setLastName(user.getUserDetails().getLastName());
        upBasicDto.setEmail(user.getEmail());
        upBasicDto.setPhone(user.getUserDetails().getPhone());
        upBasicDto.setAddressDirection(user.getUserDetails().getAddressDirection());
        upBasicDto.setCity(user.getUserDetails().getCity());
        upBasicDto.setState(user.getUserDetails().getState());
        upBasicDto.setZipcode(user.getUserDetails().getZipcode());
        return upBasicDto;
    }
}
