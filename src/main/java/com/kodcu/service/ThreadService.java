package com.kodcu.service;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Created by usta on 25.12.2014.
 */
@Component
public class ThreadService {

    private final ExecutorService threadPollWorker;
    private final ScheduledExecutorService scheduledWorker;
    private final ConcurrentHashMap<String, Buff> buffMap;
    private final Semaphore uiSemaphore;
    private final ExecutorService singleExecutor;


    public ThreadService() {
        scheduledWorker = Executors.newSingleThreadScheduledExecutor();
        threadPollWorker = Executors.newWorkStealingPool(16);
        singleExecutor = Executors.newSingleThreadExecutor();
        uiSemaphore = new Semaphore(1);
        buffMap = new ConcurrentHashMap<>();
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        return scheduledWorker.schedule(runnable, delay, timeUnit);
    }

    // Runs Task in background thread pool
    public <T> Future<?> runTaskLater(Runnable runnable) {

        Task<T> task = new Task<T>() {
            @Override
            protected T call() throws Exception {
                runnable.run();
                return null;
            }
        };

        task.exceptionProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.nonNull(newValue)) {
                newValue.printStackTrace();
            }
        });

        return threadPollWorker.submit(task);
    }

    // Runs task in JavaFX Thread
    public void runActionLater(Consumer<ActionEvent> consumer) {
        runActionLater(() -> {
            consumer.accept(null);
        });
    }


    // Runs task in JavaFX Thread
    public void runActionLater(final Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            try {
                uiSemaphore.acquire();
                Platform.runLater(() -> {
                    try {
                        runnable.run();
                        releaseUiSemaphore();
                    } catch (Exception e) {
                        releaseUiSemaphore();
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                releaseUiSemaphore();
                throw new RuntimeException(e);
            }
        }
    }

    private void releaseUiSemaphore() {
        singleExecutor.submit(() -> {
            uiSemaphore.release();
        });
    }

    public void runActionLater(Runnable runnable, boolean force) {
        if (force) {
            Platform.runLater(runnable);
        } else {
            runActionLater(runnable);
        }
    }

    public void start(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public Executor executor() {
        return threadPollWorker;
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
//            logger.error("Error in Thread#sleep", e);
        }
    }


    public Buff buff(String id) {
        buffMap.putIfAbsent(id, new Buff(this));
        return buffMap.get(id);
    }

}
