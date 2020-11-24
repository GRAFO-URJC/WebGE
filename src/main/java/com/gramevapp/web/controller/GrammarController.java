package com.gramevapp.web.controller;

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

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Controller
public class GrammarController {

    @Autowired
    private UserService userService;

    @Autowired
    private GrammarRepository grammarRepository;

    @Autowired
    private ExperimentService experimentService;

    Logger logger = Logger.getLogger(GrammarController.class.getName());
    @GetMapping(value = "/grammar/grammarRepository")
    public String grammarRepository(Model model) {
        User user = userService.getLoggedInUser();
        List<Grammar> grammarList = grammarRepository.findByUserId(user.getId());

        model.addAttribute("grammarList", grammarList);
        model.addAttribute("user", user);


        return "grammar/grammarRepository";
    }


    @PostMapping(value = "/grammar/grammarRepoSelected", params = "deleteGrammar")
    public
    @ResponseBody
    Long expRepoSelectedDelete(@RequestParam("grammarId") String grammarId) {
        Long idGrammar = Long.parseLong(grammarId);

        try {
            grammarRepository.deleteById(idGrammar);
        } catch (DataIntegrityViolationException e) {
            logger.warning(String.valueOf(e.getCause() instanceof org.hibernate.exception.ConstraintViolationException));
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                return (long) -1;
            }
        }

        return idGrammar;
    }


    @PostMapping(value = "/grammar/grammarDetail")
    public String editGrammar(Model model, @RequestParam("grammarId") String grammarId, boolean existed) {

        User user = userService.getLoggedInUser();
        long idGrammar = Long.parseLong(grammarId);

        Grammar gr = new Grammar();
        if (idGrammar != -1) {
            gr = grammarRepository.findGrammarById(idGrammar);
        }

        model.addAttribute("grammar", gr);
        model.addAttribute("user", user);
        model.addAttribute("existed", existed);

        return "grammar/grammarDetail";
    }



    @PostMapping(value = "/grammar/saveGrammar")
    public String saveGrammar(Model model, @ModelAttribute("grammar") Grammar gr) {
        User user = userService.getLoggedInUser();
        gr.setCreationDate(new Timestamp(new Date().getTime()));
        if(grammarRepository.findGrammarByGrammarNameAndUserId(gr.getGrammarName(), user.getId()) == null) {
            grammarRepository.save(gr);
            return grammarRepository(model);
        }
        return editGrammar(model, "-1", true);
    }

    @PostMapping(value = "/grammar/ajaxSaveGrammar")
    @ResponseBody
    public Boolean ajaxSaveGrammar(@RequestParam("grammarName") String grammarName,
                                  @RequestParam("grammarDescription") String grammarDescription,
                                  @RequestParam("grammarContent") String grammarContent
    ) {
        boolean existed = true;
        User user = userService.getLoggedInUser();
        Grammar grammar = grammarRepository.findGrammarByGrammarNameAndUserId(grammarName, user.getId());
        if (grammar == null) {
            existed = false;
            grammar = new Grammar(grammarName, grammarDescription, grammarContent);
            grammar.setUserId(user.getId());
            grammarRepository.save(grammar);
        }
        return existed;
    }


}
