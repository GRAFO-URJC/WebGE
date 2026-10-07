package com.gramevapp.web.service;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.Run;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

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
    volatile boolean terminate = false;
    volatile boolean terminate2 = false;

    private void takeRunsFromQueue() { //th
        while (!terminate || !runsQueue.isEmpty()) {
            try {
                Run run = runsQueue.poll(250, TimeUnit.MILLISECONDS);
                if (run != null) {
                    runService.saveRun(run);
                }
            } catch (InterruptedException e) {
                if (!terminate) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void takeDiagramsFromQueue() { //th2
        while (!terminate2 || !diagramDataqueue.isEmpty()) {
            try {
                DiagramData diagramData = diagramDataqueue.poll(250, TimeUnit.MILLISECONDS);
                if (diagramData != null) {
                    diagramDataService.saveDiagram(diagramData);
                }
            } catch (InterruptedException e) {
                if (!terminate2) {
                    Thread.currentThread().interrupt();
                    return;
                }
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
        synchronized (this) {
            if (terminate2) {
                throw new IllegalStateException("Diagram persistence is shutting down.");
            }
            try {
                diagramDataqueue.put(diagramData);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while queuing diagram persistence.", e);
            }
        }
    }

    public void saveRunAsync(Run run) {
        synchronized (this) {
            if (terminate) {
                throw new IllegalStateException("Run persistence is shutting down.");
            }
            try {
                runsQueue.put(run);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while queuing run persistence.", e);
            }
        }
    }

    @PreDestroy
    public void onEnd() {
        synchronized (this) {
            terminate = true;
            terminate2 = true;
        }
        th.interrupt();
        th2.interrupt();
        joinWriter(th);
        joinWriter(th2);
    }

    private void joinWriter(Thread writer) {
        boolean interrupted = false;
        while (writer.isAlive()) {
            try {
                writer.join();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

}
