package com.gramevapp.web.service;

import com.engine.algorithm.RunnableExpGramEv;
import com.gramevapp.web.model.Run;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class ReportsListenerService {
    private RunService runService;
    private Logger logger;

    public ReportsListenerService(RunService userService) {
        this.runService = userService;
        this.logger = Logger.getLogger(ReportsListenerService.class.getName());
    }

    // Run that threw an exception.
    private void runExceptionThrew(Run run, Exception exception) {
        run.setStatus(Run.Status.FAILED);
        run.setExecReport(run.getExecReport() + "\nUncaught exception: " + exception);
        String warningMsg = "Uncaught exception: " + exception;
        logger.warning(warningMsg);
        runService.saveRun(run);
    }

    @RabbitListener(queues = MQConfig.REPORTS_QUEUE)
    public void listener(ReportRabbitmqMessage message) {
        Run run = message.getRun();
        Exception ex = message.getException(); // puede fallar, salta un warning
        String code = message.getCode();

        // Enhanced java switch.
        switch (code) {
            case "run-exception" -> runExceptionThrew(run, ex);
            //case "stop" -> stopRun(run, elementToRun);
            default -> logger.warning("Wrong case in ReportsListenerService");
        }
    }

}
