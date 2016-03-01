package com.lambdaworks.examples.basicusage;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BasicAsyncApp {
    public static void main(String[] args) {
        // https://github.com/mp911de/lettuce/wiki/Asynchronous-API-%284.0%29
        RedisClient client = RedisClient.create("redis://localhost");
        StatefulRedisConnection<String, String> connection = client.connect();
        RedisAsyncCommands<String, String> commands = connection.async();
        Executor sharedExecutor = Executors.newFixedThreadPool(2);
        try {
            RedisFuture<String> futureSet = commands.set("key", "3");
            futureSet.await(3000, TimeUnit.MILLISECONDS);
            System.out.println(futureSet.get());
            RedisFuture<Long> future = commands.incr("key");
            Consumer<Long> c = new Consumer<Long>() {
                @Override
                public void accept(Long value) {
                    System.out.println(value);
                }
            };
            future.thenAcceptAsync(c, sharedExecutor);
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
