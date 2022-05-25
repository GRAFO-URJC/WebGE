package com.gramevapp.web.service;

import com.engine.algorithm.RunnableExpGramEv;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.repository.GrammarRepository;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

// Receiver
@Component
public class RabbitListener {
    private Logger logger;
    private ExperimentService experimentService;
    private SaveDBService saveDBService;
    private RunService runService;
    private GrammarRepository grammarRepository;
    private UserService userService;

    private final String NUM_THREADS = "2";

    //boolean autoAck = true;

    public RabbitListener(ExperimentService experimentService, SaveDBService saveDBService, RunService runService
            , GrammarRepository grammarRepository, UserService userService) {
        this.logger = Logger.getLogger(RabbitListener.class.getName());
        this.experimentService = experimentService;
        this.saveDBService = saveDBService;
        this.runService = runService;
        this.grammarRepository = grammarRepository;
        this.userService = userService;
    }

    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = MQConfig.QUEUE, concurrency = NUM_THREADS)
    public void listener(RunnableExpGramEvWrapper message) {
        Long runId = message.getRunId();
        Run run = runService.findByRunId(runId);
        logger.warning("Id sacado del service: "+ run.getId());
        RunnableExpGramEv elementToRun = message.getRunnable();
        Thread.UncaughtExceptionHandler h = (th, ex) -> {
            // Esto no vale para nada pues no se entera el service
            run.setStatus(Run.Status.FAILED);
            run.setExecReport(run.getExecReport() + "\nUncaught exception: " + ex);
            String warningMsg = "Uncaught exception: " + ex;
            logger.warning(warningMsg);
        };

        // No me gusta nada, revisar despues.
        Thread.currentThread().setUncaughtExceptionHandler(h);
        elementToRun.run();
    }
}
