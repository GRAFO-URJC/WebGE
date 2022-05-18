package com.gramevapp.web.service;

import com.engine.algorithm.CallableExpGramEv;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gramevapp.web.model.Run;
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
    public static Map<Long, Future<Void>> runToFuture = new HashMap<>();
    public static Map<Long, CallableExpGramEv> runToCallable= new HashMap<>();
    public static Map<Long, List<Future<Void>>> expIdToFutureList= new HashMap<>();
    private Logger logger = Logger.getLogger(CallablesSubmiter.class.getName());

    public static List<Future<Void>> futuresList = new ArrayList<>();

    //public static final String RECEIVE_MESSAGE_METHOD_NAME = "receiveMessage";

//    public CallablesSubmiter() {
//        this.threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()/2);
//        this.logger = Logger.getLogger(CallablesSubmiter.class.getName());
//    }

    public Future<Void> accept(CallableExpGramEvWrapper callableExpGramEvWrapper) {
        CallableExpGramEv callable = callableExpGramEvWrapper.getCallable();
        try {
            callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }


        Long runId = callableExpGramEvWrapper.getRunId();
        Long expId = callableExpGramEvWrapper.getExpId();
        Future<Void> future = ThreadPoolExperimentRunnerService.threadPool.submit(callable);
        runToFuture.put(runId, future);
        runToCallable.put(runId, callable);
        futuresList.add(future);
        expIdToFutureList.put(expId, futuresList);
        return future;
    }

    @RabbitListener(queues = MQConfig.QUEUE)
    public void listener(CallableExpGramEvWrapper message) {
        logger.warning("He entrado a receiveMessage id: "+message.getExpId());
        accept(message);
        logger.info("Callable submited to threadpool");
    }
}
