package com.gramevapp.web.controller;

import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.Grammar;
import com.gramevapp.web.model.User;
import com.gramevapp.web.repository.GrammarRepository;
import com.gramevapp.web.service.ExperimentService;
import com.gramevapp.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Controller
public class GrammarController {

    @Autowired
    private UserService userService;

    @Autowired
    private GrammarRepository grammarRepository;

    @Autowired
    private ExperimentService experimentService;


    @RequestMapping(value = "/grammar/grammarRepository", method = RequestMethod.GET)
    public String grammarRepository(Model model) {
        User user = userService.getLoggedInUser();
        List<Grammar> grammarList = grammarRepository.findByUserId(user.getId());

        model.addAttribute("grammarList", grammarList);
        model.addAttribute("user", user);

        return "grammar/grammarRepository";
    }


    @RequestMapping(value = "/grammar/grammarRepoSelected", method = RequestMethod.POST, params = "deleteGrammar")
    public
    @ResponseBody
    Long expRepoSelectedDelete(@RequestParam("grammarId") String grammarId) {
        Long idGrammar = Long.parseLong(grammarId);

        try {
            grammarRepository.deleteById(idGrammar);
        } catch (DataIntegrityViolationException e) {
            System.out.println(e.getCause() instanceof org.hibernate.exception.ConstraintViolationException);
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                return (long) -1;
            }
        }

        return idGrammar;
    }


    @RequestMapping(value = "/grammar/grammarDetail", method = RequestMethod.POST)
    public String editGrammar(Model model, @RequestParam("grammarId") String grammarId) {

        User user = userService.getLoggedInUser();
        long idGrammar = Long.parseLong(grammarId);

        Grammar gr = new Grammar();
        if (idGrammar != -1) {
            gr = grammarRepository.findGrammarById(idGrammar);
        }

        model.addAttribute("grammar", gr);
        model.addAttribute("user", user);

        return "grammar/grammarDetail";
    }

    @RequestMapping(value = "/grammar/saveGrammar", method = RequestMethod.POST)
    public String saveGrammar(Model model, @ModelAttribute("grammar") Grammar gr) {
        gr.setCreationDate(new Timestamp(new Date().getTime()));
        grammarRepository.save(gr);
        return grammarRepository(model);
    }

    @RequestMapping(value = "/grammar/ajaxSaveGrammar", method = RequestMethod.POST)
    @ResponseBody
    public String ajaxSaveGrammar(@RequestParam("grammarName") String grammarName,
                                  @RequestParam("grammarDescription") String grammarDescription,
                                  @RequestParam("grammarContent") String grammarContent
                                  ) {
        User user = userService.getLoggedInUser();
        Grammar grammar= new Grammar(grammarName,grammarDescription,grammarContent);
        grammar.setUserId(user.getId());
        grammarRepository.save(grammar);
        return "";
    }
}
