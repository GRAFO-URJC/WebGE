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
    Boolean terminate = false;
    Boolean terminate2 = false;

    @PostConstruct
    public void onInit() {

        diagramDataqueue = new ArrayBlockingQueue<>(1000);
        runsQueue = new ArrayBlockingQueue<>(1000);
        th = new Thread() {
            @Override
            public void run() {

                while (!terminate) {
                    if (!runsQueue.isEmpty()) {
                        try {
                            runService.saveRun(runsQueue.take());

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (runsQueue.isEmpty() && th.isInterrupted()) {
                        terminate = true;
                    }
                }


            }
        };

        th2 = new Thread() {
            @Override
            public void run() {
                while (!terminate2) {
                    if (!diagramDataqueue.isEmpty()) {
                        try {
                            diagramDataService.saveDiagram(diagramDataqueue.take());

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (diagramDataqueue.isEmpty() && th2.isInterrupted()) {
                        terminate2= true;
                    }
                }
            }
        };

        th.start();
        th2.start();

    }

    public void saveDiagramDataAsync(DiagramData diagramData) {
        try {
            diagramDataqueue.put(diagramData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void saveRunAsync(Run run) {
        try {
            runsQueue.put(run);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void onEnd() {
        th.interrupt();
        th2.interrupt();
    }

}
