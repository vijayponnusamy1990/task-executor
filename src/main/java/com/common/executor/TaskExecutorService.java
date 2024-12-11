package com.common.executor;


import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;

public class TaskExecutorService implements TaskExecutor{

    private final int maxConcurrency;
    private final ExecutorService executorService;
    private final BlockingQueue<TaskHolder<?>> blockingQueue;
    private final Map<UUID, Semaphore> groupLocks;
    private boolean isRunning;

    public TaskExecutorService(int n){
        if(n <= 0){
            throw new IllegalArgumentException("Max concurrency can't be less than zero");
        }
        maxConcurrency = n;
        executorService = Executors.newFixedThreadPool(maxConcurrency);
        blockingQueue = new ArrayBlockingQueue<>(maxConcurrency);
        groupLocks = new ConcurrentHashMap<>();
        isRunning = true;
        start();
    }

    @Override
    public <T> CompletableFuture<T> submitTask(Task<T> task) {
        Objects.requireNonNull(task, "Task can not be null");

        CompletableFuture<T> completableFuture = new CompletableFuture();
        TaskHolder<T> taskHolder = new TaskHolder(task, completableFuture);
        blockingQueue.offer(taskHolder);
        return completableFuture;
    }

    private void start(){
        for(int i = 0; i< maxConcurrency ; i++){
            executorService.submit(() -> {
                while (isRunning){
                    try{
                        TaskHolder<?> taskHolder = blockingQueue.poll(500, TimeUnit.MILLISECONDS);
                        if (taskHolder != null) {
                            executeTask(taskHolder);
                        }
                    }catch (InterruptedException e){
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
    }

    private <T> void executeTask(TaskHolder<T> taskHolder){
        Task<T> task = taskHolder.task;
        CompletableFuture<T> future = taskHolder.future;
        Semaphore semaphore = groupLocks.computeIfAbsent(task.taskGroup().groupUUID(), k-> new Semaphore(1));
        try {
            semaphore.acquire();
            try {
                T result = task.taskAction().call();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }finally {
                semaphore.release();
                if (!semaphore.hasQueuedThreads()) {
                    groupLocks.remove(task.taskGroup().groupUUID(), semaphore);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.completeExceptionally(e);
        }
    }

    public void shutdown(){
        isRunning = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private record TaskHolder<T>(Task<T> task, CompletableFuture<T> future) {
    }
}
