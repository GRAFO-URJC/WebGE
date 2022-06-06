package com.gramevapp.web.service.listener;

import com.engine.algorithm.RunnableExpGramEv;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

// Listener
@Component
public class RabbitListenerService {
    private Logger logger;
    private RunService runService;
    private DiagramDataService diagramDataService;
    private RabbitTemplate rabbitTemplate;



    private static final String NUM_THREADS = "2";

    public RabbitListenerService(RunService runService, DiagramDataService diagramDataService
            , RabbitTemplate rabbitTemplate) {
        this.logger = Logger.getLogger(RabbitListenerService.class.getName());
        this.runService = runService;
        this.diagramDataService = diagramDataService;
        this.rabbitTemplate = rabbitTemplate;
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
            ReportRabbitmqMessage message = new ReportRabbitmqMessage(run, ex, "run-exception");
            rabbitTemplate.convertAndSend(MQConfig.EXCHANGE ,MQConfig.REPORT_ROUTING_KEY, message);
        }
    }

    @RabbitListener(queues = MQConfig.RUNS_QUEUE, concurrency = NUM_THREADS)
    public void listener(QueueRabbitMqMessage message) {
        Long runId = message.getRunId();
        Run run = runService.findByRunId(runId);
        RunnableExpGramEv elementToRun = message.getRunnable();
        String code = message.getCode();

        // Enhanced java switch.
        switch (code) {
            case "run" -> startRun(run, elementToRun);
            case "stop" -> stopRun(run, elementToRun);
            default -> logger.warning("Wrong case in RabbitListenerService");
        }
    }
}
