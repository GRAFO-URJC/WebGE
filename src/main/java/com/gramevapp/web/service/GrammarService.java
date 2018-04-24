package com.gramevapp.web.service;

import com.gramevapp.web.repository.GrammarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GrammarService {

    @Autowired
    private GrammarRepository grammarRepository;



}
