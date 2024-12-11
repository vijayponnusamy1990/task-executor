package com.common.executor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TaskExecutorMainApp {
    public static void main(String[] args){
        TaskExecutorService executorService = new TaskExecutorService(4);
        TaskGroup group1 = new TaskGroup(UUID.randomUUID());
        TaskGroup group2 = new TaskGroup(UUID.randomUUID());
        Task<Integer> task1 = new Task<>(
                UUID.randomUUID(),
                group1,
                TaskType.READ,
                () -> {
                    Thread.sleep(1000);
                    return 1;
                }
        );
        Task<Integer> task2 = new Task<>(
                UUID.randomUUID(),
                group2,
                TaskType.WRITE,
                () -> {
                    Thread.sleep(500);
                    return 2;
                }
        );

        Task<Integer> task3 = new Task<>(
                UUID.randomUUID(),
                group1,
                TaskType.READ,
                () -> {
                    Thread.sleep(100);
                    return 1;
                }
        );
        Task<Integer> task4 = new Task<>(
                UUID.randomUUID(),
                group1,
                TaskType.READ,
                () -> {
                    Thread.sleep(10);
                    return 1;
                }
        );
        Task<Integer> task5 = new Task<>(
                UUID.randomUUID(),
                group1,
                TaskType.READ,
                () -> {
                    Thread.sleep(150);
                    return 5;
                }
        );

        try {
            // Submit tasks asynchronously using CompletableFuture

            CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
                try {
                    return executorService.submitTask(task1).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Error executing task 1", e);
                }
            });
            CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
                try {
                    return executorService.submitTask(task2).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Error executing task 2", e);
                }
            });

            CompletableFuture<Integer> future3 = CompletableFuture.supplyAsync(() -> {
                try {
                    return executorService.submitTask(task3).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Error executing task 2", e);
                }
            });

            CompletableFuture<Integer> future4 = CompletableFuture.supplyAsync(() -> {
                try {
                    return executorService.submitTask(task4).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Error executing task 2", e);
                }
            });

            CompletableFuture<Integer> future5 = CompletableFuture.supplyAsync(() -> {
                try {
                    return executorService.submitTask(task5).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Error executing task 2", e);
                }
            });


            // Register callbacks to print results when available
            future1.thenAccept(result -> System.out.println("Task 1 result: " + result))
                    .exceptionally(throwable -> {
                        System.err.println("Error in Task 1: " + throwable.getMessage());
                        return null;
                    });

            future2.thenAccept(result -> System.out.println("Task 2 result: " + result))
                    .exceptionally(throwable -> {
                        System.err.println("Error in Task 2: " + throwable.getMessage());
                        return null;
                    });
            future3.thenAccept(result -> System.out.println("Task 3 result: " + result))
                    .exceptionally(throwable -> {
                        System.err.println("Error in Task 3: " + throwable.getMessage());
                        return null;
                    });
            future4.thenAccept(result -> System.out.println("Task 4 result: " + result))
                    .exceptionally(throwable -> {
                        System.err.println("Error in Task 4: " + throwable.getMessage());
                        return null;
                    });
            future5.thenAccept(result -> System.out.println("Task 5 result: " + result))
                    .exceptionally(throwable -> {
                        System.err.println("Error in Task 5: " + throwable.getMessage());
                        return null;
                    });
        }catch (Exception e){

        }


        executorService.shutdown();
    }
}

