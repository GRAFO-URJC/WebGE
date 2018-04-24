package com.gramevapp.web.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

@Controller
public class HomeController {
    @GetMapping("/")
    public String root() throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        //ProcessBuilder builder = new ProcessBuilder("java -jar /resource/Main.jar");
        //Process process = builder.start();

        //Runtime r = Runtime.getRuntime();
        //Process p = null;
        //p = r.exec(new String[] { "cmd", "/c", "start /resources/Main.jar" });

        /*Compile class
        javac HelloWorld/Main.java

        Run class
        java -cp . HelloWorld.Main

        Create a JAR file
        jar cfme Main.jar Manifest.txt HelloWorld.Main HelloWorld/Main.class

        Run a JAR file
        java -jar Main.jar*/

        String cmd = "java -jar D:/Universidad/2017-2018/TFG/SpringSecurity/Example/gramevApp/resources/Main.jar";
        //String cmd = "java -jar ..\\resources\\Main.jar";
        // Run a java app in a separate system process
        Runtime.getRuntime().exec(cmd);

        /*String fileName = "D:/Universidad/2017-2018/TFG/SpringSecurity/Example/gramevApp/resources/Data.txt";

        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print("Some String");
        printWriter.printf("Product name is %s and its price is %d $", "iPhone", 1000);
        printWriter.close();*/

        // https://stackoverflow.com/questions/26101738/why-is-the-anonymoususer-authenticated-in-spring-security
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/user";
        }

        return "index";
    }

    @GetMapping("/kitchen")
    public String bootstrapKitchen() {
        return "bootStrap/kitchen";
    }

}