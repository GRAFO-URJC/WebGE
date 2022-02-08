package com.gramevapp.web.service;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.Run;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service("saveDBService")
public class SaveDBService {

    BlockingQueue<DiagramData> diagramDataqueue;
    BlockingQueue<Run> runsQueue;

    @Autowired
    RunService runService;

    @Autowired
    DiagramDataService diagramDataService;

    Thread th;
    Thread th2;
    boolean terminate = false;
    boolean terminate2 = false;

    private void takeRunsFromQueue() { //th
        while(!terminate) {
            try {
                if (runsQueue.isEmpty() && th.isInterrupted()) {
                    return;
                }
                runService.saveRun(runsQueue.take());

            } catch (InterruptedException e) {
                e.printStackTrace();
                terminate = true;
                Thread.currentThread().interrupt();
            }
        }
    }

    private void takeDiagramsFromQueue() { //th2
        while(!terminate2) {
            try {
                if (diagramDataqueue.isEmpty() && th2.isInterrupted()) {
                    return;
                }
                diagramDataService.saveDiagram(diagramDataqueue.take());

            } catch (InterruptedException e) {
                e.printStackTrace();
                terminate2 = true;
                Thread.currentThread().interrupt();
            }
        }
    }

    @PostConstruct
    public void onInit() {

        diagramDataqueue = new ArrayBlockingQueue<>(1000);
        runsQueue = new ArrayBlockingQueue<>(1000);
        th = new Thread(this::takeRunsFromQueue);
        th2 = new Thread(this::takeDiagramsFromQueue);

        th.start();
        th2.start();

    }

    public void saveDiagramDataAsync(DiagramData diagramData) {
        try {
            diagramDataqueue.put(diagramData);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void saveRunAsync(Run run) {
        try {
            runsQueue.put(run);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void onEnd() {
        th.interrupt();
        th2.interrupt();
    }

}
