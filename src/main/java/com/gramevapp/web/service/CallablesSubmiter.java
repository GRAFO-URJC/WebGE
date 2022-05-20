package com.gramevapp.web.service;

import com.engine.algorithm.CallableExpGramEv;
import com.engine.algorithm.RunnableExpGramEv;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.model.User;
import com.gramevapp.web.repository.GrammarRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

// Receiver
@Component
public class CallablesSubmiter {
    //private ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()/2);
    //public static Map<Long, Future<Void>> runToFuture = new HashMap<>();
    //public static Map<Long, CallableExpGramEv> runToCallable= new HashMap<>();
    //public static Map<Long, List<Future<Void>>> expIdToFutureList= new HashMap<>();
    private Logger logger;
    private ExperimentService experimentService;
    private SaveDBService saveDBService;
    private RunService runService;
    private GrammarRepository grammarRepository;
    private UserService userService;

    //public static List<Future<Void>> futuresList = new ArrayList<>();

    //public static final String RECEIVE_MESSAGE_METHOD_NAME = "receiveMessage";

    public CallablesSubmiter(ExperimentService experimentService, SaveDBService saveDBService, RunService runService
            ,GrammarRepository grammarRepository, UserService userService) {
        this.logger = Logger.getLogger(CallablesSubmiter.class.getName());
        this.experimentService = experimentService;
        this.saveDBService = saveDBService;
        this.runService = runService;
        this.grammarRepository = grammarRepository;
        this.userService = userService;
    }

//    public Future<Void> accept(CallableExpGramEvWrapper callableExpGramEvWrapper) {
//        CallableExpGramEv callable = callableExpGramEvWrapper.getCallable();
//        try {
//            callable.call();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        Long runId = callableExpGramEvWrapper.getRunId();
//        Long expId = callableExpGramEvWrapper.getExpId();
//        Future<Void> future = ThreadPoolExperimentRunnerService.threadPool.submit(callable);
//        runToFuture.put(runId, future);
//        runToCallable.put(runId, callable);
//        futuresList.add(future);
//        expIdToFutureList.put(expId, futuresList);
//        return future;
//    }

    @RabbitListener(queues = MQConfig.QUEUE)
    public void listener(RunnableExpGramEvWrapper message) {
        Long runId = message.getRunId();
        Run run = runService.findByRunId(runId);
        logger.warning("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
        logger.warning(String.valueOf(message.getRunId()==null));
        logger.warning(String.valueOf(message.getExpId()==null));
        logger.warning(String.valueOf(message.getRunnable()==null));
        if (run != null) {
            logger.warning("Id sacado del service: "+ run.getId());
            RunnableExpGramEv elementToRun = message.getRunnable();
            Thread.UncaughtExceptionHandler h = (th, ex) -> {
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
}
