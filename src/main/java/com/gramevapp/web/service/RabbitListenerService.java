package com.gramevapp.web.service;

import com.engine.algorithm.RunnableExpGramEv;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.repository.GrammarRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

// Receiver
@Component
public class RabbitListenerService {
    private Logger logger;
    private ExperimentService experimentService;
    private SaveDBService saveDBService;
    private RunService runService;
    private GrammarRepository grammarRepository;
    private UserService userService;
    private DiagramDataService diagramDataService;


    private final String NUM_THREADS = "2";

    //boolean autoAck = true;

    public RabbitListenerService(ExperimentService experimentService, SaveDBService saveDBService, RunService runService
            , GrammarRepository grammarRepository, UserService userService, DiagramDataService diagramDataService) {
        this.logger = Logger.getLogger(RabbitListenerService.class.getName());
        this.experimentService = experimentService;
        this.saveDBService = saveDBService;
        this.runService = runService;
        this.grammarRepository = grammarRepository;
        this.userService = userService;
        this.diagramDataService = diagramDataService;
    }

    private void stopRun(Run run, RunnableExpGramEv runnable) {
        runnable.stopExecution();
        run.getDiagramData().setStopped(true);
        diagramDataService.saveDiagram(run.getDiagramData());
        run.setStatus(Run.Status.STOPPED);
        runService.saveRun(run);
    }

    private void startRun(Run run, RunnableExpGramEv runnable) {
        try {
            runnable.run();
        }catch (Exception ex) {
            run.setStatus(Run.Status.FAILED);
            run.setExecReport(run.getExecReport() + "\nUncaught exception: " + ex);
            String warningMsg = "Uncaught exception: " + ex;
            logger.warning(warningMsg);
            runService.saveRun(run);
        }
    }

    @RabbitListener(queues = MQConfig.QUEUE, concurrency = NUM_THREADS)
    public void listener(RunnableExpGramEvWrapper message) {
        Long runId = message.getRunId();
        Run run = runService.findByRunId(runId);
        RunnableExpGramEv elementToRun = message.getRunnable();
        String code = message.getCode();

        // Enhanced java switch.
        switch (code) {
            case "run" -> startRun(run, elementToRun);
            case "stop" -> stopRun(run, elementToRun);
            default -> {
                logger.warning("Wrong case in RabbitListenerService");
            }
        }
    }
}
