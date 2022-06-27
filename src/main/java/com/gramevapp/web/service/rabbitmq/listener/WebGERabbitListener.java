package com.gramevapp.web.service.rabbitmq.listener;

import com.engine.algorithm.RunnableExpGramEv;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.service.*;
import com.gramevapp.web.service.rabbitmq.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

// Listener
@Component
public class WebGERabbitListener {
    private Logger logger;
    private RunService runService;
    private DiagramDataService diagramDataService;
    private RabbitTemplate rabbitTemplate;
    private SaveDBService saveDBService;

    public WebGERabbitListener(RunService runService, DiagramDataService diagramDataService
            , RabbitTemplate rabbitTemplate, SaveDBService saveDBService) {
        this.logger = Logger.getLogger(WebGERabbitListener.class.getName());
        this.runService = runService;
        this.diagramDataService = diagramDataService;
        this.rabbitTemplate = rabbitTemplate;
        this.saveDBService = saveDBService;
    }

    private void stopRun(Run run, WebGERunnable runnable) {
        runnable.stopExecution();
        run.getDiagramData().setStopped(true);
        diagramDataService.saveDiagram(run.getDiagramData());
        run.setStatus(Run.Status.STOPPED);
        runService.saveRun(run);
    }

    private void startRun(Run run, WebGERunnable runnable) {
        try {
            runnable.run();
        }catch (Exception ex) {
            ReportRabbitmqMessage message = new ReportRabbitmqMessage(run, ex, "run-exception");
            rabbitTemplate.convertAndSend(MQConfig.EXCHANGE ,MQConfig.REPORT_ROUTING_KEY, message);
        }
    }

    @RabbitListener(queues = MQConfig.RUNS_QUEUE)
    public void listener(QueueRabbitMqMessage message) {
        Long runId = message.getRunId();
        Run run = runService.findByRunId(runId);
        WebGERunnableUtils utils = message.getRunnable();
        String code = message.getCode();
        WebGERunnable elementToRun = new WebGERunnable(utils, runService, saveDBService, rabbitTemplate);

        // Enhanced java switch.
        switch (code) {
            case "run" -> startRun(run, elementToRun);
            case "stop" -> stopRun(run, elementToRun);
            default -> logger.warning("Wrong case in RabbitListenerService");
        }
    }
}
