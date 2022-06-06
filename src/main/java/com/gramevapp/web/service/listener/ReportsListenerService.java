package com.gramevapp.web.service.listener;

import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.MQConfig;
import com.gramevapp.web.service.ReportRabbitmqMessage;
import com.gramevapp.web.service.RunService;
import com.gramevapp.web.service.SaveDBService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class ReportsListenerService {
    private RunService runService;
    private SaveDBService saveDBService;
    private Logger logger;

    public ReportsListenerService(RunService userService, SaveDBService saveDBService) {
        this.runService = userService;
        this.saveDBService = saveDBService;
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

    // Run that finished successfully
    private void successRun(Run run) {
        saveDBService.saveRunAsync(run);
    }


    @RabbitListener(queues = MQConfig.REPORTS_QUEUE)
    public void listener(ReportRabbitmqMessage message) {
        Run run = message.getRun();
        Exception ex = message.getException();
        String code = message.getCode();

        // Enhanced java switch.
        switch (code) {
            case "run-exception" -> runExceptionThrew(run, ex);
            case "finish" -> successRun(run);
            default -> logger.warning("Wrong case in ReportsListenerService");
        }
    }

}
